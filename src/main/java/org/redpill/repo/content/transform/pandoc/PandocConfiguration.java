package org.redpill.repo.content.transform.pandoc;

import java.util.ArrayList;
import java.util.List;

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
    
    List<String> command = new ArrayList<String>();
    
    command.add(_pandocExe);
    command.add("--from");
    command.add("${from_format}");
    command.add("--to");
    command.add("${to_format}");
    command.add("--output");
    command.add("${target}");
    command.add("${source}");
    command.add("--variable");
    command.add("papersize:\"${papersize}\"");
    command.add("--variable");
    command.add("geometry:top=${margin_top},bottom=${margin_bottom},left=${margin_left},right=${margin_right}");

    executer.setCommand(command.toArray(new String[command.size()]));

    return executer;
  }

}
