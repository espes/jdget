JC = javac
JFLAGS =


SOURCEPATHS = \
	AppWorkUtils/src \
	jdownloader/browser/src \
	jdownloader/trunk/src \
	jdownloader/MyJDownloaderClient/src

JARS = \
	AppWorkUtils/libs/jackson-core.jar \
	AppWorkUtils/libs/jackson-mapper.jar \
	jdownloader/trunk/ressourcen/libs/sevenzipjbinding.jar \
	jdownloader/trunk/ressourcen/libs/Filters.jar \
	jdownloader/trunk/ressourcen/libs/bcprov-jdk15on-147.jar \
	AppWorkUtils/ant/joda-time-1.6.2.jar \
	jdownloader/trunk/ressourcen/libs/cobra.jar \
	jdownloader/trunk/ressourcen/libs/js.jar \
	jdownloader/trunk/ressourcen/libs/savemytube.jar


classpathify = $(subst $(eval) ,:,$(wildcard $1))

JDGet.class: JDGet.java
	$(JC) $(JFLAGS) \
		-sourcepath $(call classpathify,$(SOURCEPATHS)) \
		-classpath $(call classpathify,$(JARS)) \
		JDGet.java


.PHONY: clean
clean:
	find $(SOURCEPATHS) -name '*.class' -exec rm {} \;
	rm -f JDGet.class