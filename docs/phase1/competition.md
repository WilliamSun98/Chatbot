# Competition

Based on the description, there are 2 types of products that are satisfied:

* an information retrieval system
* a question-answering system

## Information Retrieval System

Information retrieval systems usually share these characteristics:

* more like a search engine
* key words are needed
* returns all related info based on query

#### Comparing to `Google Search`

Google Search results are very broad as it collects information from the entire internet, and our product should give 
answers that are more specific to FinTech. The users of our chat bot will be more interested in FinTech, investment,  
etc.

## Question-Answering System

Question-answering systems have these following properties in common:

* typically, one question -> one answer
* query/question should be well structured
* system filters all results, and returns the most relevant one

#### Comparing to `IBM Watson`

IBM Watson always asks users to have well structured sentences, but our chat bot should allow unstructured text.
Our users should be able to type keywords, such as `startup company support Toronto` to specify that they  
are looking for support for startup companies in Toronto, and there should be no need to  
type in full sentences.
IBM Watson builds their chat bot to talk like a real human,  
which is usually the reason why most chatbots do not have query support.

#### Comparing to `Skype bots`

Skype bots support key word query.
Compared to other bots, Skype bots support most of our client's requirements, but similar to  
Google Search, they cannot support FinTech-specific answers because its database has 
more life related materials.
Therefore, we could choose similar chatbot frameworks, and modify them to use the FinTech database. 
