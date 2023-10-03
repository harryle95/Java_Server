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

```bash
make load_balancer
```

To set up `agg_server`:

```bash
make agg_server
```

Note that you should run either the load balancer or the agg server. The default option in makefile uses 4567 for port, so if you use both at the same time, a port is binded exception will be thrown.

To set up `get_client`:

```bash
make get_client
```

To set up `content_server`:

```bash
make content_server
```

Please note that the server uses default values, so if you want to change default value, please modify them accordingly in the `Makefile`.

## To manually test the program (Optional):

### Installing Dependencies

Please skip this part if you do not have permission to install packages on the current computer.

To allow for flexible testing, I have developed a program called `PyMake`. You will need Python >= 3.10 on your current machine,
so please download that if you want to use the program.

You will then need to install the package from `PyPI`:

```
pip install PythonMake
```

### Running tests:


#### Content Server

Content server takes the following arguments:
- `hostname`: defaults to "127.0.0.1"
- `port`: defaults to 4567
- `rootDir`: defaults to `src/resources/WeatherData`
- `fileName`: defaults to `SingleEntry/Adelaide_2023-07-15_16-00-00.txt`
- `logging`: whether to log the messages to file for viewing. Defaults to false

If you want to change any of the default argument, just provide a flag and modify a value:

For example, if you want to change the port:

```bash
pymake run content_server --port 4568
```

If you want to change the rootDir:

```bash
pymake run content_server --rootDir .
```

If you want to select another file to send to AggregationServer:

```bash
pymake run content_server --fileName <fileName>
```

#### GETClient:
Get client takes the following arguments:
- `hostname`: defaults to "127.0.0.1"
- `port`: defaults to 4567
- `stationID`: required
- `logging`: whether to log the messages to file for viewing. Defaults to false

To run get client from the command line:

```agsl
pymake run get_client --stationID <stationID>
```

#### Aggregation server:

Aggregation server target  takes by default:
- `port`: defaults to 4567
- `logging`: whether to log the messages to file for viewing. Defaults to false

```bash
pymake run agg_server
```

To run aggregation server with a user-input port value:

```bash
pymake run agg_server --port <port>
```

To run aggregation server with logging:

```bash
pymake run agg_server --logging
```

#### Load Balancer:

Load balancer uses the same arguments as Aggregation Server. To run Load Balancer with a different port value:

```bash
pymake run load_balancer --port <port>
```

