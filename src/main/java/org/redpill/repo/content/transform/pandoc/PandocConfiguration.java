package org.redpill.repo.content.transform.pandoc;

import org.alfresco.util.exec.RuntimeExec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PandocConfiguration {

  @Value("${pandoc.exe}")
  private String _pandocExe;

  @Bean(name = "transformer.Pandoc.CheckCommand")
  public RuntimeExec checkCommand() {
    RuntimeExec checkCommand = new RuntimeExec();

    String[] command = { _pandocExe, "--version" };

    checkCommand.setCommand(command);

    return checkCommand;
  }

  @Bean(name = "transformer.Pandoc.Executer")
  public RuntimeExec executer() {
    RuntimeExec executer = new RuntimeExec();

    String[] command = { _pandocExe, "--from=${from_format}", "--to=${to_format}", "--output=${target}", "${source}" };

    executer.setCommand(command);

    return executer;
  }
  
}
