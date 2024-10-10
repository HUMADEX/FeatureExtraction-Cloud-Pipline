# pip install flask
from flask import Flask, request, send_file, Response
# pip install flask-restful
from flask_restful import Resource, Api
import io
import zipfile
import time
import pathlib
import os
import glob
import json
import subprocess


import numpy as np
import pandas as pd
import cv2
import face_recognition
import moviepy as mp
import moviepy.editor as moviepy
from moviepy.video.io.VideoFileClip import VideoFileClip

from comet_ml import Experiment

import multiprocessing

# Create an experiment with your api key:
experiment = Experiment(api_key="xxx")


app = Flask(__name__)
api = Api(app)

# import video_check
class MRASTCheck(Resource):
    def get(self):
        return {'mrast_check': 'ready'}

    def post(self):        
        # code goes here
        #data = request.get_json(force=True)
        #print("data", data)
        f = request.files["file"]
        f.save(f.filename)
        print(f.filename)
        fn = f.filename

        #video_name = data["video_name"]

        video_name = fn.split(".")[0]  # Split by "." and take the first part
        print("mrast check video_name"+video_name)

        asr_text_result = "empty"
        asr_text_result = request.form.get("asr_text_result")
        print("mrast check asr_text_result", asr_text_result)

        # Create the full path for the new folder
        output_path = os.path.join('/path-to-output-folder/', video_name)
        print("output_path: "+output_path)
        
        # Create the new folder
        os.makedirs(output_path, exist_ok=True)
        
        # Unzip the contents into the new folder
        with zipfile.ZipFile(f.filename, 'r') as zip_ref:
            zip_ref.extractall(path=output_path)
        
        # Check if there is no text in transcription
        
        if not asr_text_result.strip():
            print("asr_text_result is empty")
            return {'mrast_check': 'asr_text_result is empty'}, 400
                
        # Check if video lenght is less than x seconds
        
        filename= '{}.mov'.format(video_name)
        clip = VideoFileClip(r"{0}".format(filename))
        duration_video = clip.duration

        minimum_length_in_seconds = 16
        if duration_video < minimum_length_in_seconds:
            print('Duration of the video is below '+str(minimum_length_in_seconds)+' seconds, not long enough for analysis')
            return {'mrast_check': 'duration of the video is below '+str(minimum_length_in_seconds)+' seconds, not long enough for analysis'}, 400          
                    
        # Process the asr_text_result further if it's not empty
        print('Duration of the video is ok')

        # Check if there is no face detected in the video_name
        
        # The model has an accuracy of 99.38% on the Labeled Faces in the Wild benchmark.
        # https://face-recognition.readthedocs.io/en/latest/index.html          
                
               
        cap = cv2.VideoCapture(output_path+'/'+video_name+'.mov')
        controlling_duration = 10  # The duration in seconds to check for a face
        frames_processed = 0  # Track the number of frames processed
        
        while True:
            ret, frame = cap.read()
            if not ret:  # Check if frame reading is successful
                break
        
            # Only process every xth frame
            every_x_frame = 18
            if frames_processed % every_x_frame == 0:
                # Convert image from BGR to RGB
                rgb_frame = frame[:, :, ::-1]
        
                # Find faces in the frame
                face_locations = face_recognition.face_locations(rgb_frame)
        
                if face_locations:
                    print('Face detected!')
                    cap.release()  # Close video capture if face is found
                    break
        
            frames_processed += 1
            duration_in_seconds = frames_processed / cap.get(cv2.CAP_PROP_FPS)
            print("duration_in_seconds_for_each_"+str(every_x_frame)+"_frame: " + str(duration_in_seconds))
        
            if duration_in_seconds > controlling_duration:
                print('Face not detected within', controlling_duration, 'seconds')
                cap.release()  # Close video capture after timeout
                return {'mrast_check': 'face is not detected'}, 400
        
        cap.release()
            
        return {'mrast_check': 'everything ok'}, 200   

api.add_resource(MRASTCheck, '/mrast_check')

# video processing pipeline
class Features(Resource):
    def get(self):
        return {'features': 'ready'}

    def post(self):
        start_time = time.time()
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("START--------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        # Extract .zip files code goes here

        print(pathlib.Path().resolve())
        #print(request.data)
        f = request.files["file"]
        f.save(f.filename)
        print(f.filename)
        fn = f.filename

        video_name = fn.split(".")[0]  # Split by "." and take the first part
        print("video_name"+video_name)

        # Retrieve the additional form data
        asr_text_result = request.form.get("asr_text_result")
        print("ASR Text Result:", asr_text_result)

        # Create the full path for the new folder
        output_path = os.path.join('/path-to-output-folder/', video_name)
        print("output_path: "+output_path)

        # Create the new folder
        os.makedirs(output_path, exist_ok=True)

        # Unzip the contents into the new folder
        with zipfile.ZipFile(f.filename, 'r') as zip_ref:
            zip_ref.extractall(path=output_path)


        results_output_path = os.path.join('/path-to-output-folder-for-features/', video_name)
        print("results_output_path: "+results_output_path)

        # Create the new folder
        os.makedirs(results_output_path, exist_ok=True)
        end_time_extracting = time.time()
        duration_extracting = end_time_extracting - start_time
        print("---Extracting and preparing received files: "+str(duration_extracting)+"---")

        '''def convert_video_to_wmv(input_file_path, output_file_path):
            cmd = [
                "ffmpeg",
                "-y",
                "-i", input_file_path,
                "-preset", "veryfast",  # Use a faster preset to reduce conversion time
                "-c:v", "wmv2",
                "-c:a", "wmav2",
                output_file_path
            ]

            try:
                process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                stdout, stderr = process.communicate()
                exit_value = process.returncode

                if exit_value == 0:
                    print(f"Video conversion to WMV successful: {output_file_path}")
                else:
                    print("Video conversion to WMV failed")
                    print(stderr.decode())
            except Exception as e:
                print(f"An error occurred: {e}")

        # Example usage:
        input_file = output_path+"/"+video_name+".mov"
        output_file = output_path+"/"+video_name+".wmv"
        start_time_ffmpeg = time.time()
        print("Starting video conversion to .wmv")
        convert_video_to_wmv(input_file, output_file)
        end_time_ffmpeg = time.time()
        duration_ffmpeg = end_time_ffmpeg - start_time_ffmpeg
        print("--- FFMPEG running time:"+ str(duration_ffmpeg) + " seconds ---")      
        '''

        '''# Define the commands to run
        commands = [
            'python /your-path/MRAST_Observable_Cues_Speech4.py "{}"'.format(fn),
            'python /your-path/MRAST_Observable_Cues_Visual4.py "{}"'.format(fn),
            'python /your-path/Linguistic_Processor/MRASTFramework/MRASTFRamework.py "{}" "{}"'.format(fn, asr_text_result),
            'python /your-path/stanza_en_symptoms.py "{}" "{}"'.format(fn, asr_text_result)
        ]

        # Run the commands in parallel
        processes = [subprocess.Popen(command, shell=True) for command in commands]

        # Wait for all processes to complete
        for process in processes:
            process.wait()
        '''
        # Define the commands to run
        command1 = 'python /your-path/MRAST_Observable_Cues_Speech4.py "{}"'.format(fn)
        command2 = 'python /your-path/MRAST_Observable_Cues_Visual4.py "{}"'.format(fn)
        command3 = '/your-path/Linguistic_Processor/MRASTFramework/MRASTFRamework.py "{}" "{}"'.format(fn, asr_text_result)
        command4 = 'python /your-path/stanza_en_symptoms.py "{}" "{}"'.format(fn, asr_text_result)

        # Run the first command
        process1 = subprocess.Popen(command1, shell=True)

        # Run the other commands immediately in parallel
        process3 = subprocess.Popen(command3, shell=True)
        process4 = subprocess.Popen(command4, shell=True)

        # Wait for the first command to finish
        process1.wait()

        # Once the first command is done, run the second command
        process2 = subprocess.Popen(command2, shell=True)

        # Optionally, wait for the remaining processes to complete
        process2.wait()
        process3.wait()
        process4.wait()

        os.system('cp /your-path/Linguistic_Processor/MRASTFramework/summary-new/'+video_name+'_linguistic.json /path-to-output-folder-for-features/'+video_name)

        start_time_files = time.time()
        directory = results_output_path
        zip_name = video_name+'-results.zip'
        with zipfile.ZipFile(zip_name, 'w') as zip_file:
            for foldername, subfolders, filenames in os.walk(directory):
                for filename in filenames:
                    file_path = os.path.join(foldername, filename)
                    zip_file.write(file_path, os.path.relpath(file_path, directory))
        
        with open('/path-to-output-folder-for-files/'+zip_name, 'rb') as f:
            data = f.read()
        end_time = time.time()
        duration = end_time - start_time_files
        print("---Storing results: "+str(duration)+"---")
        print('All files are zipped and transferred')

        end_time2 = time.time()
        duration2 = end_time2 - start_time
        print("-------------------------------------------------------")
        print("---Processing finished for video: "+str(video_name)+"---")
        print("-------------------------------------------------------")
        print("--- Total running time: "+ str(duration2)  + "seconds ---")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("END----------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")
        print("-------------------------------------------------------")

        # Changed line below
        return Response(data,
                        mimetype='application/zip',
                        headers={'Content-Disposition': 'attachment;filename='+zip_name})           


api.add_resource(Features, '/features')   

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5004, use_reloader=False)

    
