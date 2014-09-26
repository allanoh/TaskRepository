<?php

require_once __DIR__ . "/vendor/autoload.php";


use Illuminate\Database\Capsule\Manager as Capsule;
use Silex\Application;
use Symfony\Component\HttpFoundation\Request;
use Gogo\Data;
use Symfony\Component\HttpFoundation\JsonResponse;
use Illuminate\Events\Dispatcher;
use Illuminate\Container\Container;
use Philo\Blade\Blade;


$views = __DIR__ . '/views';
$cache = __DIR__ . '/cache';

//application setup
$app = new Application();
$blade = new Blade($views, $cache);

//database setup


$capsule = new Capsule;

//mysql config
$capsule->addConnection([
    'driver' => 'mysql',
    'host' => 'localhost',
    'database' => 'gogo',
    'username' => 'root',
    'password' => '',
    'charset' => 'utf8',
    'collation' => 'utf8_unicode_ci',
    'prefix' => '',
]);

$capsule->setEventDispatcher(new Dispatcher(new Container()));

$capsule->bootEloquent();
//end db setup

$app->get('/', function () use ($blade, $app) {

//show the main page located in views/dashboard.php
    return $blade->view()->make("dashboard");

});
//respond to uploads
$app->post('/upload', function (Request $request) {

    /**@var \Symfony\Component\HttpFoundation\File\UploadedFile $csv */
    $csv = $request->files->get('csv');

//create new csv reader handle
    $reader = new \League\Csv\Reader(new SplFileObject($csv->getRealPath()));
//    get headers
    $headers = $reader->fetchOne();

//get all headers in lower case
    $smallCaseHeaders = array_map(function ($column) {
        return strtolower($column);
    }, $headers);

//add a filter to get all columns greater than 0 (excludes the headers)
    $reader->addFilter(function ($row, $index) {
        return $index > 0;
    })->setLimit(4000);

//    save csv to mysql columns
    $session = \Gogo\Data::max('session') + 1;

    $rows = $reader->fetchAll();

    $dataToInsert = array();

    foreach ($rows as $row) {
        $myRow = array();
        array_walk($row, function ($value, $key) use (&$myRow, $smallCaseHeaders) {
            $myRow[$smallCaseHeaders[$key]] = floatval($value);
        });
        $myRow['session'] = $session;
        array_push($dataToInsert, $myRow);
    }
    Data::insert($dataToInsert);

    return new JsonResponse($rows);
});

//get the session id's
$app->get('/sessions', function () {
//    get distinct data
    $sessions = Data::distinct('session')->get(array('session'));

    return new JsonResponse($sessions->toArray());
});
//get data
$app->get('/sessions/{id}',function($id){
//get all session data
    $data = Data::whereSession($id)->get();

    return new JsonResponse($data->toArray());
});


$app['debug'] = true;

$app->run();