# Update on the way to manage fake API by JSON Server

### Describing the old way to serve fake API

To serve fake APIs, all we needed was to create a json file in the same folder
as the controller, following the syntax required by the documentation of JSON
server (check this [link](https://github.com/typicode/json-server)), 
and serve it with this command (Example):

```http
  json-server --watch mock-exchanges.json
```

The back-end is going to communicate with the JSON Server via this URL:

```http
  http://localhost:3000/exchanges
```

### The Issue

This is so fine if you're trying to serve data only for the feature you're 
implementing or fixing. The problem is in the test environment we won't be 
be able to serve multiple JSON files on the same port, we would need 
to specify each file with its port, which is so overwhelming. So we need 
to serve all of the data all at once.

### The Solution

A working little solution was implemented, which is to regroup all the data 
in one file, called fakeAPI.json which shares the same root with this file.
This file will contain all the data following the syntax of JSON Server, 
and serve it all in one command. 

From now on, to add new data, you can just open the fakeAPI.json file, and
add the data you want respecting the syntax so that your data can be served 
right.

N.B: It is preferred that you keep the pieces of Data that you add to 
the fakeAPI in a separate file too, we might need them in case we wanted
to reformulate data, or in case the main file has been corrupted.
