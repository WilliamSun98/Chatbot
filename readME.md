# DFI Chatbot

## Overview

This project is a chatbot created for the purpose of providing answers to
questions about the Financial Technology (FinTech) industry. It works with IBM
Watson to provide answers to the user when they ask certain general questions,
and also uses a webcrawler and lucene indexer to provide links and files when
answers cannot be found with Watson.

## Design Decisions and Philosophies

### Design Philosophies

1. Simplicity over robustness: This is a chatbot, and people use chatbots when
   they would like to ask quick questions and receive quick responses. Because of
   this we wanted to keep the interface of the chatbot simple and not cluter it
   with options that the users were most likely not going to use.
2. Speed over precision: Indexing is not a simple job and requires time to
   perform, but the purpose of the chatbot is to simulate human conversation, so
   we would rather have fast responses that are not quite perfect than wait a long
   time for the "perfect" response.

### Design Decisions

1. Creating an interface for the crawler: Since we had two types of crawlers
   (one for web results, and one for jobs) that both used the same functions, an
   interface was the clear way to go.
2. Builder design pattern for the crawlers, and files: There are quite a few
   parameters for the crawlers and files, so a builder was used to avoid confusion.
3. Using custom file object: We had particular use cases from the files we were
   using for indexing and needed them to store information we would use to index
   properly, so a custom object was created for our needs.

## Running the Application

### What you need

- A computer
- 2 open terminals
- An internet browser (Google Chrome recommended)
- Node and npm

### Steps

1. Navigate to the \<other folders\>/c01summer2019groupproject13/crawler folder
   in command line
2. Run the following command: mvn install
3. Run: mvn spring-boot:run
4. In a separate terminal navigate to the /c01summer2019groupproject13/front-end
   folder
5. Run: npm install
6. Run: ng serve
7. Open browser to http://localhost:4200
8. Follow on screen stuff

## Folder Structure

In the main folder there are 6 folders to consider:

1. **Connector**: contains files for connecting to the frontend, and creating the
   session for the current user.
2. **Crawler**: the files for the crawlers, there are 2 crawlers in this project,
   one for searching the web for files and answers, and one for searching for
   jobs. Since they use the same functions, there is also an interface for them.
3. **FileBuilder**: since we needed to use custom object for the files we were
   storing to have all the functionality we wanted, this folder contains the
   builder for the file objects
4. **Filter**: files that handle the filtering options that come from the frontend
5. **Indexer**: files for saving and retrieving from the index, uses lucene
6. **UrlConnector**: files for the REST controller, send data to the frontend
