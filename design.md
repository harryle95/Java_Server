## Elements 

- Aggregation Server: 
  - Responds to client requests 
  - Accepts weather updates 
  - Removes stale data. Data is stale if: 
    - The content server is no longer in contact (30 s time window)
    - When the data is too old (Not one of the most recent 20 updates)
- Client Server: 
  - Send HTTP GET request to the server and display data 
- Content Server: 
  - Read from local environment to JSON file
  - Send HTTP PUT requests to the server, updating JSON data

## Behaviour 

- Multiple clients may send multiple GET requests. Responds must be appropriate if there is an interleaved PUT. 
- Multiple content server simultaneous PUT requests will be serialised based on Lamport clocks. 
- Aggregation server removes contents that are not in use in the last 30s. 

## Questions: 
- How are the content feeds identified? Assuming the GET request contains feed id information. 
- If each item is identified by the content server it receives information from, can two content servers modify the same content? 
- What does it mean by "one of the most recent 20 updates"? Does update here mean
  - PUT requests from the content server where the data comes from 
  - PUT requests from all servers
  - Both PUT and GET requests.  
- Can we assume the update operation is atomic? That is if the server crashes before an update, it will be restored to the state before an update happens. 
- Will the server send anything if a crash happend? Will the content server resend the PUT update if it does not receive an ACK?
