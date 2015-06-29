# static-code-analysis-report
How to Transform the Results from Findbugs, Checkstyle and PMD into a single HTML Report with XSLT 2.0 and Java?

## Usage
1. Clone the repository
2. Build the project with

  ```shell
  mvn clean package
  ```
3. run it 

  ```shell
  java -jar target/scautil.jar findbugs.xml checkstyle.xml pmd.xml result.html
  ```

[![Build Status](https://travis-ci.org/maddingo/static-code-analysis-report.svg?branch=master)](https://travis-ci.org/maddingo/static-code-analysis-report)
