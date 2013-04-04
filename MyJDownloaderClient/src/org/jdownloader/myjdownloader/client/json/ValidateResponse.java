package org.jdownloader.myjdownloader.client.json;

public class ValidateResponse {

    private boolean successful = false;

    public ValidateResponse(/* Storable */) {
    }

    /**
     * @return the successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * @param successful
     *            the successful to set
     */
    public void setSuccessful(final boolean successful) {
        this.successful = successful;
    }
}
