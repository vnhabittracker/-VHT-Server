<?php

// Headers
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Access-Control-Allow-Headers,Content-Type,Access-Control-Allow-Methods, Authorization, X-Requested-With');

include_once '../../config/config.php';
include_once '../../models/Reminder.php';

// Instantiate DB & connect
$database = new Database();
$db = $database->connect();

// Instantiate
$reminder = new Reminder($db);

// Get raw posted data
$data = json_decode(file_get_contents("php://input"));
$reminder->reminder_id = $data->server_id;
$reminder->habit_id = $data->habit_id;
$reminder->remind_start_time = $data->remind_start_time;
$reminder->remind_end_time = $data->remind_end_time;
$reminder->repeat_type = $data->repeat_type;
$reminder->reminder_description = $data->reminder_description;

if ($reminder->create()) {
    echo json_encode(
        array(
            'result' => '1',
            'id' => $reminder->reminder_id
        )
    );
} else {
    echo json_encode(
        array(
            'result' => '0'
        )
    );
}

?>
