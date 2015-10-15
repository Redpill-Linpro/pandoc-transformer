package org.redpill.repo.content.transform.pandoc.it;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.exec.RuntimeExec;
import org.junit.Test;

public class ParameterTest {

  @Test
  public void testFoobar() {
    RuntimeExec exec = new RuntimeExec();

    exec.setCommand(new String[] { "-V papersize:\"${papersize}\"" });

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("papersize", "a4paper");
    
    String[] command = exec.getCommand(properties);
    
    for (String s : command) {
      System.out.println(s);
    }
  }

}
