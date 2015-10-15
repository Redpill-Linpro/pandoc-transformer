# Pandoc Transformer for Alfresco

Writing documentation in a word processor can be a tedious job. Writing it in Markdown on the other hand is nice :) Since there's no previewer for Markdown documents in Alfresco, a transformer is the next best thing. This projects aim is to create a simple transformer that creates PDF documents out of Markdown documents. To achieve this it uses the excellent [Pandoc](http://www.pandoc.org) command line binary.

## Building

Follow these steps to download the source code and to build the project.

1. `git clone https://github.com/oakman/pandoc-transformer`
2. `cd pandoc-transfomer`
3. `mvn clean package`

This builds a .jar file that can then be used in an Alfresco installation. To build with integration testing, issue `mvn -Pit,purge clean verify` instead.

## Installation

### Ubuntu

1. Copy the .jar file to the `tomcat/webapps/alfresco/WEB-INF/lib` folder.
2. Install pandoc. Don't use the package manager version, it's probably too old. Download the .deb package instead from [here](http://pandoc.org/installing.html) and install it with `dpkg -i <package-file>`.  
3. In order for PDF's to be created, LaTeX is used. Install it with `sudo apt-get install texlive` or `sudo apt-get install texlive-full`.

### Mac OS X

1. Copy the .jar file to the `tomcat/webapps/alfresco/WEB-INF/lib` folder.
2. Install pandoc. The homebrew version is nice, install it with `brew install pandoc`.  
3. In order for PDF's to be created, LaTeX is used. There's a huge package that needs to be installed, do this with homebrew `brew cask install mactex`. It could also work with a stripped down version, BasicTeX. To install that one, use `brew cask install basictex` instead of the previous command.

### Windows

Who in it's right mind uses Windows? If you happen to do this, you're on your own.
