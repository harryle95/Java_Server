## How to run the program

To compile source code excluding tests:

```bash
make compile_src
```

To compile test code:

```bash
make compile_test
```

To run all tests:

```bash
make run_test 
```

## To manually test the program:

To set up `load_balanacer`:

By default, load balancer uses port 4567

```bash
make load_balancer [PORT=<port_number>]
```

To set up `agg_server`:

By default, load balancer uses port 4567

```bash
make agg_server [PORT=<port_number>]
```

Note that you should run either the load balancer or the agg server. The default option in makefile uses 4567 for port,
so if you use both at the same time, a port is binded exception will be thrown. Load balancer 
offers fault tolerance and should be used. 

To set up `get_client`:

By default client connects to server at port 4567 and request station id with value 5000

```bash
make get_client [PORT=<port_number>] [CONTENT_ID=<station_id>]
```

To set up `content_server`:

```bash
make content_server [PORT=<port_number>] [CONTENT_FILE=<file_name>] [RESOURCE_DIR=<path_to_file>]
```

By default, content_server connects to host server at port 4567, select files from `src/src/resources/WeatherData/Composite`
and upload file named `Adelaide_2023-07-15_16-30-00.txt`.

To upload all resources for testing:

```bash
make upload_all
```