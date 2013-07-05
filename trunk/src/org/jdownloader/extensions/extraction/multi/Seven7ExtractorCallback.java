package org.jdownloader.extensions.extraction.multi;

import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ICryptoGetTextPassword;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;

import org.appwork.utils.ReusableByteArrayOutputStream;
import org.appwork.utils.StringUtils;
import org.jdownloader.extensions.extraction.FileSignatures;

public class Seven7ExtractorCallback implements IArchiveExtractCallback, ICryptoGetTextPassword {

    private final AtomicBoolean        passwordfound;
    private final ISevenZipInArchive   inArchive;
    private final String               password;
    private SignatureCheckingOutStream signatureOutStream;
    private boolean                    optimized;

    public Seven7ExtractorCallback(ISevenZipInArchive inArchive, AtomicBoolean passwordfound, String password, ReusableByteArrayOutputStream buffer, long maxPWCheckSize, FileSignatures filesignatures, boolean optimized) {
        this.passwordfound = passwordfound;
        this.inArchive = inArchive;
        this.password = password;
        this.optimized = optimized;
        signatureOutStream = new SignatureCheckingOutStream(passwordfound, filesignatures, buffer, maxPWCheckSize, optimized);
    }

    Boolean skipExtraction = null;

    @Override
    public void setTotal(long l) throws SevenZipException {
    }

    @Override
    public void setCompleted(long l) throws SevenZipException {
    }

    @Override
    public ISequentialOutStream getStream(int i, ExtractAskMode extractaskmode) throws SevenZipException {
        if (passwordfound.get()) throw new SevenZipException("Password found");
        if (skipExtraction != null && skipExtraction == false && signatureOutStream.getWritten() == 0) {
            //
            throw new SevenZipException("Password wrong");
        }
        skipExtraction = (Boolean) inArchive.getProperty(i, PropID.IS_FOLDER);
        if (skipExtraction || extractaskmode != ExtractAskMode.EXTRACT) {
            skipExtraction = true;
            return null;
        }
        String name = (String) inArchive.getProperty(i, PropID.PATH);
        if (StringUtils.isEmpty(name)) {
            skipExtraction = false;
            return null;
        }
        skipExtraction = false;
        signatureOutStream.reset();
        long size = (Long) inArchive.getProperty(i, PropID.SIZE);
        signatureOutStream.setSignatureLength(name, size);
        return signatureOutStream;
    }

    @Override
    public void prepareOperation(ExtractAskMode extractaskmode) throws SevenZipException {
        int hh = 1;
    }

    @Override
    public void setOperationResult(ExtractOperationResult extractoperationresult) throws SevenZipException {
        int hh = 1;
    }

    @Override
    public String cryptoGetTextPassword() throws SevenZipException {
        return password;
    }

}
