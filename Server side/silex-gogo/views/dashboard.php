<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="../../favicon.ico">

    <title>GoGo Sensor</title>

    <!-- Bootstrap core CSS -->
    <link rel="stylesheet" href="/assets/bootstrap/dist/css/bootstrap.css"/>
    <link href="/assets/bootstrap/dist/css/bootstrap-theme.css" rel="stylesheet">


    <!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
    <!--[if lt IE 9]>
    <script src="/assets/bootstrap/dist/js/ie8-responsive-file-warning.js"></script><![endif]-->
    <script src="/assets/bootstrap/dist/js/ie-emulation-modes-warning.js"></script>

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>

<body>

<div class="container">

    <!-- Static navbar -->
    <div class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                        data-target=".navbar-collapse">
                    <span class="sr-only">Menu</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#">GoGO Sensor</a>
            </div>
        </div>
        <!--/.container-fluid -->
    </div>

    <!-- Main component for a primary marketing message or call to action -->
    <div class="jumbotron">
        <h1>Gogo Signal Analyser</h1>

        <p>The gogo signal analyser is an advanced sensor analytics platform that gives you the relationship between
            your cars engine and its acceleration.
            Now lets <strong>Go Go</strong> analyze the data</p>

        <p>
            <a class="btn btn-lg btn-primary" href="#charts" role="button">View Analytics &raquo;</a>
        </p>
    </div>
    <div class="panel panel-primary">
        <div class="panel-heading">Analytics</div>
        <div class="panel-body" id="charts">
            <div class="col-md-3 pull-right text-right clearfix">
                Select Session
                <select name="sessions" id="sessions">
                    <option value="">None</option>
                </select>
            </div>
            <div id="lineChart" style="height: 300px ;clear: both">
                <div class="lead">
                    No session selected
                </div>
            </div>
        </div>
    </div>

</div>
<!-- /container -->


<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="/assets/jquery/dist/jquery.min.js"></script>
<script src="/assets/bootstrap/dist/js/bootstrap.min.js"></script>
<script src="/js/chartjs/canvasjs.min.js"></script>
<script src="/assets/spinjs/spin.js"></script>
<!--Main js files here-->
<script>
//our application starts here
    $(document).ready(function () {


//            init app
        var sessionsSelect = $('#sessions');
        $.get('/sessions').success(function (data) {
//            add sessions to select
            data.forEach(function (row) {
                $(sessionsSelect).append('<option>' + row.session + '</option>')

            });
        })
//select session change handler
        $(sessionsSelect).on('change', function () {
            console.log(this.value);
            drawChart(this.value);
        })

//        draw chart
        function drawChart(sessionId) {

            $.get('sessions/' + sessionId).success(function (data) {

                var accelerometerData = [];
//declare the columns
                var x = {
                    "name": "accelerometer x",
                    "type": "spline", showInLegend: true,
                    "dataPoints": [], "color": 'rgb(255, 127, 14)'
                }
                var y = {
                    "name": "accelerometer y",

                    "type": "spline",
                    showInLegend: true,
                    "dataPoints": [], "color": 'rgb(44, 160, 44)'
                };
                var z = {
                    "name": "accelerometer z",

                    "type": "spline", showInLegend: true,
                    "dataPoints": [], "color": 'rgb(119, 119, 255)'
                };

                var noise = {
                    "name": "Noise",

                    "type": "column", showInLegend: true,
                    "dataPoints": [], "color": '#23BFAA'
                };
//                end declare columns

                accelerometerData = [x, y, z, noise];

//add data points to the columns
                for (var i = 0; i < 20; i++) {
                    var time = new Date(data[i].time);
                    var xVal = +data[i].accelerometer_x;

                    x.dataPoints.push({x: time, y: xVal});

                    var yVal = +data[i].accelerometer_y;
                    y.dataPoints.push({x: time, y: yVal});
                    //
                    var zVal = +data[i].accelerometer_z;
                    z.dataPoints.push({x: time, y: zVal});

                    var noiseVal = +data[i].noise;
                    noise.dataPoints.push({x: time, y: noiseVal});

                }
//chart definition
              var  accelerometerChart = new CanvasJS.Chart("lineChart",
                    {
                        title: {
                            text: "Signal Analytics"
                        },
                        data: accelerometerData
                    });
//draw that chart
                accelerometerChart.render();

            });

        }
    });
</script>
</body>
</html>
