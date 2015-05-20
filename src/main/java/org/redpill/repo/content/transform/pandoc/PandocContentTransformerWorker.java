package org.redpill.repo.content.transform.pandoc;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformerHelper;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.content.transform.ExplictTransformationDetails;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component("transformer.worker.Pandoc")
public class PandocContentTransformerWorker extends ContentTransformerHelper implements ContentTransformerWorker {

  public static final String MIMETYPE_MARKDOWN1 = "text/x-markdown";
  public static final String MIMETYPE_MARKDOWN2 = "text/x-web-markdown";

  private static final Logger LOG = Logger.getLogger(PandocContentTransformerWorker.class);

  @Autowired
  @Qualifier("transformer.Pandoc.CheckCommand")
  private RuntimeExec _checkCommand;

  @Autowired
  @Qualifier("transformer.Pandoc.Executer")
  private RuntimeExec executer;

  @Autowired
  @Qualifier("MimetypeService")
  private MimetypeService _mimetypeService;

  private boolean _available = false;

  private String _versionString;

  @Override
  public String getVersionString() {
    return _versionString;
  }

  @Override
  public boolean isAvailable() {
    return _available;
  }

  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    if (!isAvailable()) {
      return false;
    }

    Assert.hasText(sourceMimetype);
    Assert.hasText(targetMimetype);

    if (isMarkdown(sourceMimetype) && targetMimetype.equalsIgnoreCase(MimetypeMap.MIMETYPE_PDF)) {
      return true;
    }

    if (isMarkdown(sourceMimetype) && targetMimetype.equalsIgnoreCase(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING)) {
      return true;
    }

    if (isMarkdown(sourceMimetype) && targetMimetype.equalsIgnoreCase(MimetypeMap.MIMETYPE_HTML)) {
      return true;
    }

    if (isMarkdown(sourceMimetype) && targetMimetype.equalsIgnoreCase(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT)) {
      return true;
    }

    return false;
  }

  @Override
  public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception {
    if (!isAvailable()) {
      return;
    }

    // get mimetypes
    String sourceMimetype = getMimetype(reader);
    String targetMimetype = getMimetype(writer);

    String sourceExtension = _mimetypeService.getExtension(sourceMimetype);
    String targetExtension = _mimetypeService.getExtension(targetMimetype);

    if (StringUtils.isBlank(sourceExtension) || StringUtils.isBlank(targetExtension)) {
      throw new AlfrescoRuntimeException("Unknown extensions for mimetypes: \n" + "   source mimetype: " + sourceMimetype + "\n" + "   source extension: " + sourceExtension + "\n"
          + "   target mimetype: " + targetMimetype + "\n" + "   target extension: " + targetExtension);
    }

    // create required temp files
    File sourceFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_source_", "." + sourceExtension);
    File targetFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_target_", "." + targetExtension);

    // pull reader file into source temp file
    reader.getContent(sourceFile);

    long timeoutMs = options.getTimeoutMs();

    Map<String, String> properties = new HashMap<String, String>();

    String sourceFormat = "markdown_github";
    String targetFormat = getTargetFormat(targetMimetype);
    
    properties.put("from_format", sourceFormat);
    properties.put("to_format", targetFormat);
    properties.put("source", sourceFile.getAbsolutePath());
    properties.put("target", targetFile.getAbsolutePath());

    RuntimeExec.ExecutionResult result = executer.execute(properties, timeoutMs);

    if (result.getExitValue() != 0 && result.getStdErr() != null && result.getStdErr().length() > 0) {
      throw new ContentIOException("Failed to perform Pandoc transformation: \n" + result);
    }

    // check that the file was created
    if (!targetFile.exists() || targetFile.length() == 0) {
      throw new ContentIOException("Pandoc transformation failed to write output file");
    }
    
    // upload the output image
    writer.putContent(targetFile);
    
    // done
    if (LOG.isDebugEnabled()) {
      LOG.debug("Transformation completed: \n" + "   source: " + reader + "\n" + "   target: " + writer + "\n" + "   options: " + options);
    }
  }

  @PostConstruct
  public void postConstruct() {
    try {
      // On some platforms / versions, the -version command seems to return an
      // error code whilst still
      // returning output, so let's not worry about the exit code!
      ExecutionResult result = _checkCommand.execute();
      int exitValue = result.getExitValue();

      if (exitValue != 0) {
        throw new RuntimeException("Exit code from check command is " + exitValue);
      }

      _versionString = result.getStdOut().trim();
      
      _available = true;
    } catch (Throwable e) {
      _available = false;

      LOG.error(getClass().getSimpleName() + " not available: " + (e.getMessage() != null ? e.getMessage() : ""));

      // debug so that we can trace the issue if required
      LOG.debug(e);
    }
  }

  @Override
  public void setExplicitTransformations(List<ExplictTransformationDetails> explicitTransformations) {
    explicitTransformations.clear();

    explicitTransformations.add(new ExplictTransformationDetails(MIMETYPE_MARKDOWN1, MimetypeMap.MIMETYPE_PDF));
    explicitTransformations.add(new ExplictTransformationDetails(MIMETYPE_MARKDOWN2, MimetypeMap.MIMETYPE_PDF));
  }

  private boolean isMarkdown(String sourceMimetype) {
    return sourceMimetype.equalsIgnoreCase(MIMETYPE_MARKDOWN1) || sourceMimetype.equalsIgnoreCase(MIMETYPE_MARKDOWN2);
  }

  private String getTargetFormat(String targetMimetype) {
    Assert.hasText(targetMimetype);
    
    if (MimetypeMap.MIMETYPE_PDF.equalsIgnoreCase(targetMimetype)) {
      return "latex";
    }

    if (MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING.equalsIgnoreCase(targetMimetype)) {
      return "docx";
    }

    if (MimetypeMap.MIMETYPE_HTML.equalsIgnoreCase(targetMimetype)) {
      return "html5";
    }

    if (MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT.equalsIgnoreCase(targetMimetype)) {
      return "odt";
    }

    throw new IllegalArgumentException(String.format("The target mimetype '%s' doesn't have a corresponding target format.", targetMimetype));
  }

}
