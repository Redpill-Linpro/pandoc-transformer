package org.redpill.repo.content.transform.pandoc.it;

import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition.executer.AbstractTransformationRenderingEngine;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.redpill.repo.content.transform.pandoc.PandocContentTransformerWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PandocContentTransformerWorkerIntegrationTest extends AbstractRepoIntegrationTest {

  private static final String DEFAULT_USER = "testuser_" + System.currentTimeMillis();

  public static final QName RD_PDF = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "pdf");
  public static final QName RD_DOCX = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "docx");
  public static final QName RD_HTML = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "html");
  public static final QName RD_ODT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "odt");

  @Autowired
  @Qualifier("transformer.worker.Pandoc")
  private ContentTransformerWorker _worker;

  @Autowired
  @Qualifier("ContentService")
  private ContentService _contentService;

  @Autowired
  @Qualifier("RenditionService")
  private RenditionService _renditionService;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();

    createUser(DEFAULT_USER);

    AuthenticationUtil.setFullyAuthenticatedUser(DEFAULT_USER);
  }

  @Test
  public void testGetVersionString() {
    String version = _worker.getVersionString();

    assertNotNull(version);
  }

  @Test
  public void testIsTransformable() {
    boolean transformable = _worker.isTransformable("text/x-markdown", MimetypeMap.MIMETYPE_PDF, null);

    assertTrue(transformable);
  }

  @Test
  public void testIsNotTransformable() {
    boolean transformable = _worker.isTransformable("text/x-markdown", MimetypeMap.MIMETYPE_WORD, null);

    assertFalse(transformable);
  }

  @Test
  public void testPdfTransform() throws Exception {
    testTransform(MimetypeMap.MIMETYPE_PDF, "pdf");
  }

  @Test
  public void testDocxTransform() throws Exception {
    testTransform(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, "docx");
  }

  @Test
  public void testHtmlTransform() throws Exception {
    testTransform(MimetypeMap.MIMETYPE_HTML, "html");
  }

  @Test
  public void testOdtTransform() throws Exception {
    testTransform(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT, "odt");
  }

  @Test
  public void testPdfRender() throws Exception {
    testRender(MimetypeMap.MIMETYPE_PDF, RD_PDF, "pdf");
  }

  @Test
  public void testDocxRender() throws Exception {
    testRender(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, RD_DOCX, "docx");
  }

  @Test
  public void testHtmlRender() throws Exception {
    testRender(MimetypeMap.MIMETYPE_HTML, RD_HTML, "html");
  }

  @Test
  public void testOdtRender() throws Exception {
    testRender(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT, RD_ODT, "odt");
  }

  public void testTransform(String mimetype, String extension) throws Exception {
    SiteInfo site = createSite();

    try {
      NodeRef document = uploadDocument(site, "test.md").getNodeRef();

      ContentReader reader = _contentService.getReader(document, ContentModel.PROP_CONTENT);
      reader.setMimetype(PandocContentTransformerWorker.MIMETYPE_MARKDOWN1);

      File targetFile = TempFileProvider.createTempFile("temp_", "." + extension);
      ContentWriter writer = new FileContentWriter(targetFile);
      writer.setMimetype(mimetype);

      TransformationOptions options = new TransformationOptions();

      _worker.transform(reader, writer, options);

      assertTrue(targetFile.exists());
      assertTrue(targetFile.isFile());
      assertTrue(targetFile.length() > 1000);
    } finally {
      deleteSite(site);
    }
  }

  public void testRender(String mimetype, QName renditionName, String extension) throws Exception {
    SiteInfo site = createSite();

    try {
      NodeRef document = uploadDocument(site, "test.md").getNodeRef();

      RenditionDefinition renditionDefinition = createRenditionDefinition(mimetype, renditionName);

      NodeRef rendition = _renditionService.render(document, renditionDefinition).getChildRef();

      ContentReader reader = _contentService.getReader(rendition, ContentModel.PROP_CONTENT);

      File targetFile = TempFileProvider.createTempFile("temp_", "." + extension);

      reader.getContent(targetFile);

      assertTrue(targetFile.exists());
      assertTrue(targetFile.isFile());
      assertTrue(targetFile.length() > 1000);
    } finally {
      deleteSite(site);
    }
  }

  private RenditionDefinition createRenditionDefinition(String mimetype, QName renditionName) {
    RenditionDefinition definition = _renditionService.createRenditionDefinition(renditionName, ReformatRenderingEngine.NAME);

    definition.setTrackStatus(true);

    Map<String, Serializable> parameters = new HashMap<String, Serializable>();

    parameters.put(RenditionService.PARAM_RENDITION_NODETYPE, ContentModel.TYPE_CONTENT);

    parameters.put(AbstractRenderingEngine.PARAM_SOURCE_CONTENT_PROPERTY, ContentModel.PROP_CONTENT);
    parameters.put(AbstractRenderingEngine.PARAM_MIME_TYPE, mimetype);

    parameters.put(AbstractTransformationRenderingEngine.PARAM_TIMEOUT_MS, 300000L);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_READ_LIMIT_TIME_MS, -1L);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_MAX_SOURCE_SIZE_K_BYTES, -1L);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_READ_LIMIT_K_BYTES, -1L);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_MAX_PAGES, -1);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_PAGE_LIMIT, -1);

    definition.addParameterValues(parameters);

    return definition;
  }

}
