[<img src="https://sling.apache.org/res/logos/sling.png"/>](https://sling.apache.org)

 [![Build Status](https://builds.apache.org/buildStatus/icon?job=Sling/sling-org-apache-sling-commons-html/master)](https://builds.apache.org/job/Sling/job/sling-org-apache-sling-commons-html/job/master) [![Test Status](https://img.shields.io/jenkins/t/https/builds.apache.org/job/Sling/job/sling-org-apache-sling-commons-html/job/master.svg)](https://builds.apache.org/job/Sling/job/sling-org-apache-sling-commons-html/job/master/test_results_analyzer/) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.commons.html/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.commons.html%22) [![JavaDocs](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.commons.html.svg)](https://www.javadoc.io/doc/org.apache.sling/org.apache.sling.commons.html) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling Commons HTML Utilities

This module is part of the [Apache Sling](https://sling.apache.org) project.

current settings and their default values

* http://xml.org/sax/features/namespaces=true
* http://xml.org/sax/features/namespace-prefixes=false
* http://xml.org/sax/features/external-general-entities=false
* http://xml.org/sax/features/external-parameter-entities=false
* http://xml.org/sax/features/is-standalone=false
* http://xml.org/sax/features/lexical-handler/parameter-entities=false
* http://xml.org/sax/features/resolve-dtd-uris=true
* http://xml.org/sax/features/string-interning=true
* http://xml.org/sax/features/use-attributes2=false
* http://xml.org/sax/features/use-locator2=false
* http://xml.org/sax/features/use-entity-resolver2=false
* http://xml.org/sax/features/validation=false
* http://xml.org/sax/features/xmlns-uris=false
* http://xml.org/sax/features/xmlns-uris=false
* http://xml.org/sax/features/xml-1.1=false

default SAX features are defined here
http://www.saxproject.org/apidoc/org/xml/sax/package-summary.html

## TagSoup specific features

| Feature ID | Default | Description |
| --- | --- | --- |
| `http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons` | `false` | A value of `true` indicates that the parser will ignore unknown elements. |
| `http://www.ccil.org/~cowan/tagsoup/features/bogons-empty` | `false` | A value of `true` indicates that the parser will give unknown elements a content model of EMPTY; a value of `false`, a content model of ANY. |
| `http://www.ccil.org/~cowan/tagsoup/features/root-bogons` | `true` | A value of `true indicates that the parser will allow unknown elements to be the root of the output document. |
| `http://www.ccil.org/~cowan/tagsoup/features/default-attributes` | `true` | A value of `true` indicates that the parser will return default attribute values for missing attributes that have default values. |
| `http://www.ccil.org/~cowan/tagsoup/features/translate-colons` | `false` | A value of `true` indicates that the parser will translate colons into underscores in names. |
| `http://www.ccil.org/~cowan/tagsoup/features/restart-elements` | `true` | A value of `true` indicates that the parser will attempt to restart the restartable elements. |
| `http://www.ccil.org/~cowan/tagsoup/features/ignorable-whitespace` | `false` | A value of `true` indicates that the parser will transmit whitespace in element-only content via the SAX ignorableWhitespace callback. Normally this is not done, because HTML is an SGML application and SGML suppresses such whitespace. |
| `http://www.ccil.org/~cowan/tagsoup/features/cdata-elements` | `true` | A value of `true` indicates that the parser will process the `script` and `style` elements (or any elements with `type='cdata'` in the TSSL schema) as SGML CDATA elements (that is, no markup is recognized except the matching end-tag). |

  
