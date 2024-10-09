##################################################################################
# ######################### VISUAL FEATURES EXTRACTION  ##################
# #################################################################################

import torch
torch.cuda.empty_cache()
import time

#***********COMMON lIBRARIES*********
import numpy as np
import pandas as pd
import math
import gc
import glob
from sklearn.metrics import classification_report
from pathlib import Path
from statistics import *
from scipy.spatial import distance
ln = np.log
import json
from itertools import groupby
import collections
from pathlib import Path


#***********AUDIO lIBRARIES*********
#import audiofile
#import opensmile

#import parselmouth
#from parselmouth.praat import call, run_file

import scipy
from scipy.stats import binom
from scipy.stats import ks_2samp
from scipy.stats import ttest_ind
import os

import moviepy as mp
from moviepy.editor import *
from moviepy.video.io.VideoFileClip import VideoFileClip

#***********VIDEO lIBRARIES*********
from IPython import get_ipython
import pandas as pd, seaborn as sns
sns.set_style('white')
import matplotlib.pyplot as plt 
from scipy.signal import find_peaks

import subprocess
import multiprocessing


import os    

if torch.cuda.is_available():
    print("Running on GPU")
else:
    print("Running on CPU")
print("Current GPU device: "+str(torch.cuda.current_device()))  # Returns the current GPU device
print("Number of GPUs available: "+str(torch.cuda.device_count()))  # Returns the number of GPUs available
print("Name of the GPU: "+str(torch.cuda.get_device_name(0)))  # Returns the name of the GPU

video_name = sys.argv[1]
video_name = video_name.split(".")[0]  # Split by "." and take the first part

print('video_name:',video_name)

results_output_path = os.path.join('/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/', video_name)
print("results_output_path: "+results_output_path)
            
# Create the new folder
os.makedirs(results_output_path, exist_ok=True)

results_output_path2 = os.path.join('/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/processed/', video_name)
print("processed_results_output_path: "+results_output_path2)
            
# Create the new folder
os.makedirs(results_output_path2, exist_ok=True)


filename_root="/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/"+video_name+"/"
filename_root_output="/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/"+video_name+"/"
filename = '/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/{}/{}.mov'.format(video_name,video_name)

#***********Splitting Video into sub-videos*********

clip = VideoFileClip(r"{0}".format(filename))
duration_video = clip.duration


if duration_video < 30:
    num_subvideos = 1
else:
    num_subvideos = int(duration_video // 30)
    
    # Handle remaining part of the video
    if duration_video - (num_subvideos * 30) > 3:
        num_subvideos += 1


#***********FACIL FEATURE EXTRACTION*********

start_time_visual = time.time()

print('Facial Feature Extraction is started...')

#print("FACIL FEATURE EXTRACTION was started...") 

B3_3_Head_Amplitude_Frames_Percentage = [] 
B9_face_moving_Frames_Percentage = [] 
B9_mouth_moving_Frames_Percentage = [] 
B10_looking_down_Frames_Percentage = [] 
B11_lips_down_Frames_Percentage = [] 
B12_head_away_updown_Frames_Percentage = [] 
B12_head_away_leftright_Frames_Percentage = [] 
B12_head_away_zdirection_Frames_Percentage = [] 
B13_head_down_Frames_Percentage = [] 
B14_Rotational_Energy_Frames_small_Percentage = [] 
B14_Rotational_Energy_Frames_large_Percentage = []
B14_Rotational_Energy_Frames_higher_Percentage = []
A5_frowning_Frames_Percentage = [] 
A6_eyebrows_Frames_Percentage = [] 
A7_squeezinglip_Frames_Percentage = [] 
A8_smiling_Frames_Percentage = [] 
NeutralFrames_percentage = [] 
AngryFrames_percentage = [] 
DisgustFrames_percentage = [] 
FearFrames_percentage = [] 
HappyFrames_percentage = [] 
SadnessFrames_percentage = [] 
SurpriseFrames_percentage = [] 
NeutralFrames = [] 
AngryFrames = [] 
DisgustFrames = [] 
FearFrames = [] 
HappyFrames = [] 
SadnessFrames = [] 
SurpriseFrames = []

B9_mouth_moving_intensity_video_avg = []
B11_lips_down_intensity_video_avg = []
A5_frowning_video_avg = []
A6_eyebrows_video_avg = []
A7_squeezinglip_video_avg = []
A8_smiling_video_avg = []
B15_2_emotions_angry_intenstiy_video_avg = []
B15_3_emotions_disgust_intenstiy_video_avg = []
B15_4_emotions_fear_intenstiy_video_avg = []
B15_5_emotions_happy_intenstiy_video_avg = []
B15_6_emotions_sadness_intenstiy_video_avg = []
B15_7_emotions_surprise_intenstiy_video_avg = []

video_emotion_variability = []

B9_mouth_moving_intensity = 0 
B11_lips_down_intensity = 0 
A5_frowning_intensity = 0 
A6_eyebrows_intensity = 0
A7_squeezinglip_intensity = 0
A8_smiling_intensity = 0

B15_2_emotions_angry_intenstiy = 0
B15_3_emotions_disgust_intenstiy = 0
B15_4_emotions_fear_intenstiy = 0
B15_5_emotions_happy_intenstiy = 0
B15_6_emotions_sadness_intenstiy = 0
B15_7_emotions_surprise_intenstiy = 0

B9_face_moving_Frames = 0
B9_mouth_moving_Frames = 0
B10_looking_down_Frames = 0
B11_lips_down_Frames = 0
    
B12_head_away_updown_Frames = 0
B12_head_away_leftright_Frames = 0
B12_head_away_zdirection_Frames = 0
        
B13_head_down_Frames = 0
B14_Rotational_Energy_Frames_small = 0
B14_Rotational_Energy_Frames_large = 0
B14_Rotational_Energy_Frames_higher = 0
    
A5_frowning_Frames = 0
A6_eyebrows_Frames = 0
A7_squeezinglip_Frames = 0
A8_smiling_Frames = 0
B3_3_Head_Amplitude_Frames = 0 

NeutralFrames = 0
AngryFrames=0
DisgustFrames=0
FearFrames=0
HappyFrames=0
SadnessFrames=0
SurpriseFrames=0



A8_smiling_all_values = []

facial_features={}

facial_features['B3_3_Head_Amplitude_Frames_Percentage']= {}
facial_features['B9_face_moving_Frames_Percentage']= {}
facial_features['B9_mouth_moving_Frames_Percentage']= {}    
facial_features['B10_looking_down_Frames_Percentage']= {}
facial_features['B11_lips_down_Frames_Percentage']= {}
facial_features['B12_head_away_updown_Frames_Percentage']= {}
facial_features['B12_head_away_leftright_Frames_Percentage']= {}
facial_features['B12_head_away_zdirection_Frames_Percentage']= {}
facial_features['B13_head_down_Frames_Percentage']= {}
facial_features['B14_Rotational_Energy_Frames_small_Percentage']= {}
facial_features['B14_Rotational_Energy_Frames_large_Percentage']= {}
facial_features['B14_Rotational_Energy_Frames_higher_Percentage']= {}
facial_features['A5_frowning_Frames_Percentage']= {}
facial_features['A6_eyebrows_Frames_Percentage']= {}
facial_features['A7_squeezinglip_Frames_Percentage']= {}
facial_features['A8_smiling_Frames_Percentage']= {}
    
facial_features['NeutralFrames_percentage']= {}
facial_features['AngryFrames_percentage']= {}
facial_features['DisgustFrames_percentage']= {}
facial_features['FearFrames_percentage']= {}
facial_features['HappyFrames_percentage']= {}
facial_features['SadnessFrames_percentage']= {}
facial_features['SurpriseFrames_percentage']= {}

facial_features['B9_mouth_moving_intensity_video_avg']= {}
facial_features['B11_lips_down_intensity_video_avg']= {}
facial_features['A5_frowning_video_avg']= {}
facial_features['A6_eyebrows_video_avg']= {}
facial_features['A7_squeezinglip_video_avg']= {}
facial_features['A8_smiling_video_avg']= {}
facial_features['B15_2_emotions_angry_intenstiy_video_avg']= {}
facial_features['B15_3_emotions_disgust_intenstiy_video_avg']= {}
facial_features['B15_4_emotions_fear_intenstiy_video_avg']= {}
facial_features['B15_5_emotions_happy_intenstiy_video_avg']= {}
facial_features['B15_6_emotions_sadness_intenstiy_video_avg']= {}
facial_features['B15_7_emotions_surprise_intenstiy_video_avg']= {}

facial_features['total_emotion_variability']= {}

facial_features['A8_smiling_1_consecutive_frame']= {}
facial_features['A8_smiling_2_consecutive_frame']= {}
facial_features['A8_smiling_3_consecutive_frame']= {}
facial_features['A8_smiling_1_consecutive_frame_length']= {}
facial_features['A8_smiling_2_consecutive_frame_length']= {}
facial_features['A8_smiling_3_consecutive_frame_length']= {}
    
print(num_subvideos)


def process_subvideo(filename):
    print('process_subvideo: started')
    command = f"/Dockers/Datasets/libraries/Pipeline/OpenFace/build/bin/FeatureExtraction -f {filename} -2Dfp -aus -pose -gaze -out_dir /Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/processed/"+video_name+"/"
    print('command:',command)
    os.system(command)

#def merge_csv_files(output_dir):
#    csv_files = [os.path.join(output_dir, f) for f in os.listdir(output_dir) if f.endswith('.csv')]
#    df = pd.concat([pd.read_csv(f) for f in csv_files])
#    df.to_csv("/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/processed/"+video_name+"/"+video_name+"_output.csv", index=False)

subvideo_filenames = [
    f"/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/{video_name}/{video_name}_video_{i+1}.mov"
    for i in range(num_subvideos)
]

# Number of CPUs: 12
processes=os.cpu_count()
#processes= 4

with multiprocessing.Pool(processes) as pool:
    output_files = pool.map(process_subvideo, subvideo_filenames)

    #merge_csv_files("/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/processed/{}".format(video_name))


print("Openface processing is ended in %s seconds ---" % (time.time() - start_time_visual))  

def feature_calculation(csv_file_path):
    
    B9_mouth_moving_intensity = 0 
    B11_lips_down_intensity = 0 
    A5_frowning_intensity = 0 
    A6_eyebrows_intensity = 0
    A7_squeezinglip_intensity = 0
    A8_smiling_intensity = 0

    B15_2_emotions_angry_intenstiy = 0
    B15_3_emotions_disgust_intenstiy = 0
    B15_4_emotions_fear_intenstiy = 0
    B15_5_emotions_happy_intenstiy = 0
    B15_6_emotions_sadness_intenstiy = 0
    B15_7_emotions_surprise_intenstiy = 0

    B9_face_moving_Frames = 0
    B9_mouth_moving_Frames = 0
    B10_looking_down_Frames = 0
    B11_lips_down_Frames = 0

    B12_head_away_updown_Frames = 0
    B12_head_away_leftright_Frames = 0
    B12_head_away_zdirection_Frames = 0

    B13_head_down_Frames = 0
    B14_Rotational_Energy_Frames_small = 0
    B14_Rotational_Energy_Frames_large = 0
    B14_Rotational_Energy_Frames_higher = 0

    A5_frowning_Frames = 0
    A6_eyebrows_Frames = 0
    A7_squeezinglip_Frames = 0
    A8_smiling_Frames = 0
    B3_3_Head_Amplitude_Frames = 0 

    NeutralFrames = 0
    AngryFrames=0
    DisgustFrames=0
    FearFrames=0
    HappyFrames=0
    SadnessFrames=0
    SurpriseFrames=0
    
    total_frame = 0
    
    # Load data
    df = pd.read_csv(csv_file_path)
    
    # Remove empty spaces in column names.
    df.columns = [col.replace(" ", "") for col in df.columns] 

    
    
    total_frame = total_frame + len(df['frame'])

    Intensities = []      

    minima_intensity = 0.0

    frame_map_AUs={} 
    frame_map_AUs['frame_number']={} 

    facial_features2={}
    facial_features2['B9_face_moving']={}
    Facial_Lands_x=pd.DataFrame({'x_0':df['x_0'],'x_1':df['x_1'],'x_2':df['x_2'],'x_3':df['x_3'],'x_4':df['x_4'],'x_5':df['x_5'],'x_6':df['x_6'],'x_7':df['x_7'],'x_8':df['x_8'],'x_9':df['x_9'],'x_10':df['x_10'],                                    'x_11':df['x_11'],'x_12':df['x_12'],'x_13':df['x_13'],'x_14':df['x_14'],'x_15':df['x_15'],'x_16':df['x_16'],'x_17':df['x_17'],'x_18':df['x_18'],'x_19':df['x_19'],'x_20':df['x_20'],
                                         'x_21':df['x_21'],'x_22':df['x_22'],'x_23':df['x_23'],'x_24':df['x_24'],'x_25':df['x_25'],'x_26':df['x_26'],'x_27':df['x_27'],'x_28':df['x_28'],'x_29':df['x_29'],'x_30':df['x_30'],
                                         'x_31':df['x_31'],'x_32':df['x_32'],'x_33':df['x_33'],'x_34':df['x_34'],'x_35':df['x_35'],'x_36':df['x_36'],'x_37':df['x_37'],'x_38':df['x_38'],'x_39':df['x_39'],'x_40':df['x_40'],
                                         'x_41':df['x_41'],'x_42':df['x_42'],'x_43':df['x_43'],'x_44':df['x_44'],'x_45':df['x_45'],'x_46':df['x_46'],'x_47':df['x_47'],'x_48':df['x_48'],'x_49':df['x_49'],'x_50':df['x_50'],
                                         'x_51':df['x_51'],'x_52':df['x_52'],'x_53':df['x_53'],'x_54':df['x_54'],'x_55':df['x_55'],'x_56':df['x_56'],'x_57':df['x_57'],'x_58':df['x_58'],'x_59':df['x_59'],'x_60':df['x_60'],
                                         'x_61':df['x_61'],'x_62':df['x_62'],'x_63':df['x_63'],'x_64':df['x_64'],'x_65':df['x_65'],'x_66':df['x_66'],'x_67':df['x_67']})

    Facial_Lands_y=pd.DataFrame({'y_0':df['y_0'],'y_1':df['y_1'],'y_2':df['y_2'],'y_3':df['y_3'],'y_4':df['y_4'],'y_5':df['y_5'],'y_6':df['y_6'],'y_7':df['y_7'],'y_8':df['y_8'],'y_9':df['y_9'],'y_10':df['y_10'],
                                         'y_11':df['y_11'],'y_12':df['y_12'],'y_13':df['y_13'],'y_14':df['y_14'],'y_15':df['y_15'],'y_16':df['y_16'],'y_17':df['y_17'],'y_18':df['y_18'],'y_19':df['y_19'],'y_20':df['y_20'],
                                         'y_21':df['y_21'],'y_22':df['y_22'],'y_23':df['y_23'],'y_24':df['y_24'],'y_25':df['y_25'],'y_26':df['y_26'],'y_27':df['y_27'],'y_28':df['y_28'],'y_29':df['y_29'],'y_30':df['y_30'],
                                         'y_31':df['y_31'],'y_32':df['y_32'],'y_33':df['y_33'],'y_34':df['y_34'],'y_35':df['y_35'],'y_36':df['y_36'],'y_37':df['y_37'],'y_38':df['y_38'],'y_39':df['y_39'],'y_40':df['y_40'],
                                         'y_41':df['y_41'],'y_42':df['y_42'],'y_43':df['y_43'],'y_44':df['y_44'],'y_45':df['y_45'],'y_46':df['y_46'],'y_47':df['y_47'],'y_48':df['y_48'],'y_49':df['y_49'],'y_50':df['y_50'],
                                         'y_51':df['y_51'],'y_52':df['y_52'],'y_53':df['y_53'],'y_54':df['y_54'],'y_55':df['y_55'],'y_56':df['y_56'],'y_57':df['y_57'],'y_58':df['y_58'],'y_59':df['y_59'],'y_60':df['y_60'],
                                         'y_61':df['y_61'],'y_62':df['y_62'],'y_63':df['y_63'],'y_64':df['y_64'],'y_65':df['y_65'],'y_66':df['y_66'],'y_67':df['y_67']})

    Facial_Lands_length=len(Facial_Lands_x.columns) 

    AUs_Emotion=pd.DataFrame({'AU01_r':df['AU01_r'],'AU02_r':df['AU02_r'],'AU04_r':df['AU04_r'],'AU05_r':df['AU05_r'],'AU06_r':df['AU06_r'],'AU07_r':df['AU07_r'],
                                    'AU09_r':df['AU09_r'],'AU10_r':df['AU10_r'],'AU12_r':df['AU12_r'],'AU14_r':df['AU14_r'],'AU15_r':df['AU15_r'],'AU17_r':df['AU17_r'],'AU20_r':df['AU20_r'],
                                    'AU23_r':df['AU23_r'],'AU25_r':df['AU25_r'],'AU26_r':df['AU26_r']})

    AU_length=len(AUs_Emotion.columns)

    for i in range(len(df['frame'])):      

        facial_features2 = {}       

        frame_map={}  # B9   
        frame_map_AUs['frame_number'][i] ={}
        frame_map['AU01_r']={}; frame_map['AU02_r']={}; frame_map['AU04_r']={}; frame_map['AU05_r']={}; frame_map['AU06_r']={}; frame_map['AU07_r']={}; 
        frame_map['AU09_r']={}; frame_map['AU10_r']={}; frame_map['AU12_r']={}; frame_map['AU14_r']={};
        frame_map['AU15_r']={}; frame_map['AU17_r']={}; frame_map['AU20_r']={}; frame_map['AU23_r']={}; frame_map['AU25_r']={}; frame_map['AU26_r']={};

        facial_features2['A5_frowning']= {} 
        facial_features2['A5_frowning']['value']= {} 
        facial_features2['A5_frowning']['distribution'] = {}
        facial_features2['A5_frowning']['distribution']['AU04_r']= {} 

        facial_features2['A6_eyebrows']= {} 
        facial_features2['A6_eyebrows']['value'] = {} 
        facial_features2['A6_eyebrows']['distribution'] = {}
        facial_features2['A6_eyebrows']['distribution']['AU01_r']= {} 
        facial_features2['A6_eyebrows']['distribution']['AU02_r']= {} 
        facial_features2['A6_eyebrows']['distribution']['AU04_r']= {} 

        facial_features2['A7_squeezinglip']= {} 
        facial_features2['A7_squeezinglip']['value']= {} 
        facial_features2['A7_squeezinglip']['distribution'] = {} 
        facial_features2['A7_squeezinglip']['distribution']['AU23_r']= {} 

        facial_features2['A8_smiling']= {} 
        facial_features2['A8_smiling']['value']= {} 
        facial_features2['A8_smiling']['distribution'] = {}
        facial_features2['A8_smiling']['distribution']['AU06_r']= {} 
        facial_features2['A8_smiling']['distribution']['AU12_r']= {}

        facial_features2['B3_3_Head_Amplitude']={}
        facial_features2['B3_3_Head_Amplitude']['frame']={}

        facial_features2['B9_face_moving']= {} 
        facial_features2['B9_mouth_moving_intensity']= {} 

        facial_features2['B10_looking_down'] = {} 
        facial_features2['B10_looking_down']['value'] = {} 

        facial_features2['B11_lips_down']= {} 
        facial_features2['B11_lips_down']['value'] = {} 
        facial_features2['B11_lips_down']['distribution'] = {} 
        facial_features2['B11_lips_down']['distribution']['AU15_r'] = {} 
        facial_features2['B11_lips_down']['distribution']['AU20_r'] = {} 

        facial_features2['B12_head_away_updown']= {} 
        facial_features2['B12_head_away_updown']['value']= {} 
        facial_features2['B12_head_away_leftright']= {} 
        facial_features2['B12_head_away_leftright']['value']= {} 
        facial_features2['B12_head_away_zdirection']= {} 
        facial_features2['B12_head_away_zdirection']['value']= {} 


        facial_features2['B13_head_down']= {} 
        facial_features2['B13_head_down']['value']= {}         

        facial_features2['B14_Rotational_Energy']={}
        facial_features2['B14_Rotational_Energy']['frame']={}
        facial_features2['B14_Rotational_Energy_small']={}
        facial_features2['B14_Rotational_Energy_small']['frame']={}
        facial_features2['B14_Rotational_Energy_large']={}
        facial_features2['B14_Rotational_Energy_large']['frame']={}
        facial_features2['B14_Rotational_Energy_higher']={}
        facial_features2['B14_Rotational_Energy_higher']['frame']={}

        facial_features2['B15_1_emotions_neutral']= {}
        facial_features2['B15_1_emotions_neutral']['value'] = {}
        facial_features2['B15_1_emotions_neutral']['intenstiy'] = {}      
        facial_features2['B15_1_emotions_neutral']['distribution'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU01_r']= {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU02_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU04_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU05_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU06_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU07_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU10_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU12_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU14_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU15_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU17_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU20_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU23_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU25_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU26_r'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU28_c'] = {}
        facial_features2['B15_1_emotions_neutral']['distribution']['AU45_r'] = {}

        facial_features2['B15_2_emotions_angry'] = {}
        facial_features2['B15_2_emotions_angry']['value'] = {}
        facial_features2['B15_2_emotions_angry']['intenstiy'] = {}
        facial_features2['B15_2_emotions_angry']['distribution'] = {}
        facial_features2['B15_2_emotions_angry']['distribution']['AU04_r'] = {}
        facial_features2['B15_2_emotions_angry']['distribution']['AU05_r'] = {}
        facial_features2['B15_2_emotions_angry']['distribution']['AU07_r'] = {}
        facial_features2['B15_2_emotions_angry']['distribution']['AU10_r'] = {}
        facial_features2['B15_2_emotions_angry']['distribution']['AU17_r'] = {}
        facial_features2['B15_2_emotions_angry']['distribution']['AU23_r'] = {}
        facial_features2['B15_2_emotions_angry']['distribution']['AU25_r'] = {}
        facial_features2['B15_2_emotions_angry']['distribution']['AU26_r'] = {}

        facial_features2['B15_3_emotions_disgust'] = {}
        facial_features2['B15_3_emotions_disgust']['value'] = {}
        facial_features2['B15_3_emotions_disgust']['intenstiy'] = {}
        facial_features2['B15_3_emotions_disgust']['distribution'] = {}
        facial_features2['B15_3_emotions_disgust']['distribution']['AU09_r'] = {}
        facial_features2['B15_3_emotions_disgust']['distribution']['AU10_r'] = {}
        facial_features2['B15_3_emotions_disgust']['distribution']['AU17_r'] = {}

        facial_features2['B15_4_emotions_fear'] = {}
        facial_features2['B15_4_emotions_fear']['value'] = {}
        facial_features2['B15_4_emotions_fear']['intenstiy'] = {}
        facial_features2['B15_4_emotions_fear']['distribution'] = {}
        facial_features2['B15_4_emotions_fear']['distribution']['AU01_r'] = {}
        facial_features2['B15_4_emotions_fear']['distribution']['AU02_r'] = {}
        facial_features2['B15_4_emotions_fear']['distribution']['AU04_r'] = {}
        facial_features2['B15_4_emotions_fear']['distribution']['AU05_r'] = {}
        facial_features2['B15_4_emotions_fear']['distribution']['AU20_r'] = {}
        facial_features2['B15_4_emotions_fear']['distribution']['AU25_r'] = {}
        facial_features2['B15_4_emotions_fear']['distribution']['AU26_r'] = {}

        facial_features2['B15_5_emotions_happy'] = {}
        facial_features2['B15_5_emotions_happy']['value'] = {}
        facial_features2['B15_5_emotions_happy']['intenstiy'] = {}
        facial_features2['B15_5_emotions_happy']['distribution'] = {}
        facial_features2['B15_5_emotions_happy']['distribution']['AU06_r'] = {}
        facial_features2['B15_5_emotions_happy']['distribution']['AU12_r'] = {}

        facial_features2['B15_6_emotions_sadness'] = {}
        facial_features2['B15_6_emotions_sadness']['value'] = {}
        facial_features2['B15_6_emotions_sadness']['intenstiy'] = {}
        facial_features2['B15_6_emotions_sadness']['distribution'] = {}
        facial_features2['B15_6_emotions_sadness']['distribution']['AU01_r'] = {}
        facial_features2['B15_6_emotions_sadness']['distribution']['AU04_r'] = {}
        facial_features2['B15_6_emotions_sadness']['distribution']['AU06_r'] = {}
        facial_features2['B15_6_emotions_sadness']['distribution']['AU15_r'] = {}
        facial_features2['B15_6_emotions_sadness']['distribution']['AU17_r'] = {}

        facial_features2['B15_7_emotions_surprise'] = {}
        facial_features2['B15_7_emotions_surprise']['value'] = {}
        facial_features2['B15_7_emotions_surprise']['intenstiy'] = {}
        facial_features2['B15_7_emotions_surprise']['distribution'] = {}
        facial_features2['B15_7_emotions_surprise']['distribution']['AU01_r'] = {}
        facial_features2['B15_7_emotions_surprise']['distribution']['AU02_r'] = {}
        facial_features2['B15_7_emotions_surprise']['distribution']['AU05_r'] = {}
        facial_features2['B15_7_emotions_surprise']['distribution']['AU26_r'] = {}                


        # B14_Rotational_Energy ***************
        if i>0:
            # Angular velocity (ω = (α₂ - α₁) / t)
            # where α₁ and α₂ are two values of angles on a circle, and Δα is their difference. t is the time in which the angle change occurs. 


            magnitude_angle1 = math.sqrt(df['pose_Rx'][i] ** 2 + df['pose_Ry'][i]  ** 2 + df['pose_Rz'][i]  ** 2) # current frame
            magnitude_angle2 = math.sqrt(df['pose_Rx'][i-1] ** 2 + df['pose_Ry'][i-1]  ** 2 + df['pose_Rz'][i-1]  ** 2) # previous frame

            cosinus_gama_anglex1  = df['pose_Rx'][i] / magnitude_angle1 # in left right  direction 
            cosinus_gama_anglex2  = df['pose_Rx'][i-1] / magnitude_angle2            
            resultx1 = math.acos(cosinus_gama_anglex1) #radian - inverse cosine
            resultx2 = math.acos(cosinus_gama_anglex2) #radian - inverse cosine

            cosinus_gama_angley1  = df['pose_Ry'][i] / magnitude_angle1 # in left right  direction 
            cosinus_gama_angley2  = df['pose_Ry'][i-1] / magnitude_angle2            
            resulty1 = math.acos(cosinus_gama_angley1) #radian - inverse cosine
            resulty2 = math.acos(cosinus_gama_angley2) #radian - inverse cosine

            cosinus_gama_anglez1  = df['pose_Rz'][i] / magnitude_angle1 # in left right  direction 
            cosinus_gama_anglez2  = df['pose_Rz'][i-1] / magnitude_angle2            
            resultz1 = math.acos(cosinus_gama_anglez1) #radian - inverse cosine
            resultz2 = math.acos(cosinus_gama_anglez2) #radian - inverse cosine

            rotation_diff_x= resultx1 - resultx2
            rotation_diff_y= resulty1 - resulty2
            rotation_diff_z= resultz1 - resultz2

            ang_vel_x = rotation_diff_x / 0.04 # 25 fps 
            ang_vel_y = rotation_diff_y / 0.04
            ang_vel_z = rotation_diff_z / 0.04

            # Moment of inertia (I = m * r²) Specimen-specific data from Plaga et al. (2005). Mean of all participants Iyy (kg-cm2) 
            MOI = 148.44 

            # Rotational kinetic energy (RE = 0.5 * MOI * ω²)
            energy_x = 0.5 * MOI * ang_vel_x * ang_vel_x
            energy_y = 0.5 * MOI * ang_vel_y * ang_vel_y
            energy_z = 0.5 * MOI * ang_vel_z * ang_vel_z     

            facial_features2['B14_Rotational_Energy']['frame'][i] = energy_x + energy_y + energy_z

            if facial_features2['B14_Rotational_Energy']['frame'][i] > 50 and facial_features2['B14_Rotational_Energy']['frame'][i] < 500:
                B14_Rotational_Energy_Frames_small = B14_Rotational_Energy_Frames_small +1

            if facial_features2['B14_Rotational_Energy']['frame'][i] >= 500 and facial_features2['B14_Rotational_Energy']['frame'][i] < 5000:
                B14_Rotational_Energy_Frames_large = B14_Rotational_Energy_Frames_large +1

            if facial_features2['B14_Rotational_Energy']['frame'][i] >= 5000:
                B14_Rotational_Energy_Frames_higher = B14_Rotational_Energy_Frames_higher +1

        # B3_3_Head_Amplitude ***************
        if i>0:
            diff=0            
            for j in range(Facial_Lands_length):                
                next_frame=[Facial_Lands_x.iloc[i,j],Facial_Lands_y.iloc[i,j]]
                previous_frame=[Facial_Lands_x.iloc[i-1,j],Facial_Lands_y.iloc[i-1,j]]                
                change=ln(abs(distance.euclidean(previous_frame,next_frame)))                
                if math.isinf(change)==True:
                    change=0                
                diff=diff + change                                
            facial_features2['B3_3_Head_Amplitude']['frame'][i]=diff/Facial_Lands_length
            if facial_features2['B3_3_Head_Amplitude']['frame'][i] > 0:
                B3_3_Head_Amplitude_Frames = B3_3_Head_Amplitude_Frames +1

        # B9_face_moving ***************
        if i>0:
            change_number = 0

            for j in range(AU_length):
                AU_name='{}'.format(AUs_Emotion.iloc[:, j].name)         
                if abs(AUs_Emotion.iloc[i,j]-AUs_Emotion.iloc[i-1,j]) >= 0.01:                                   
                    frame_map[AU_name]["intensity"]= AUs_Emotion.iloc[i,j]
                    frame_map[AU_name]["change"]= 1
                    change_number = change_number + 1
                else:
                    frame_map[AU_name]["intensity"]= AUs_Emotion.iloc[i,j]
                    frame_map[AU_name]["change"]= 0           
            facial_features2['B9_face_moving'][i]=frame_map 

            if change_number > AU_length/2:
                B9_face_moving_Frames = B9_face_moving_Frames +1

            if df['AU12_r'][i] >= 1.0 or df['AU15_r'][i] >= 1.0 or df['AU20_r'][i] >= 1.0 or df['AU23_r'][i] >= 1.0 or df['AU25_r'][i] >= 1.0 or df['AU26_r'][i] >= 1.0:
                facial_features2['B9_mouth_moving_intensity'][i]= df['AU12_r'][i]+df['AU15_r'][i]+df['AU20_r'][i]+df['AU23_r'][i]+df['AU25_r'][i]+df['AU26_r'][i] 
                B9_mouth_moving_intensity = B9_mouth_moving_intensity + ((facial_features2['B9_mouth_moving_intensity'][i])/6)
                B9_mouth_moving_Frames  = B9_mouth_moving_Frames +1


        # B10_looking_down ***************

        if i>0:
            magnitude_eye1 = math.sqrt(df['gaze_0_x'][i] ** 2 + df['gaze_0_y'][i]  ** 2 + df['gaze_0_z'][i]  ** 2) # current frame
            magnitude_eye2 = math.sqrt(df['gaze_0_x'][i-1] ** 2 + df['gaze_0_y'][i-1]  ** 2 + df['gaze_0_z'][i-1]  ** 2) # previous frame

            cosinus_gama_eye1 = df['gaze_0_y'][i] / magnitude_eye1 # in up-down direction 
            cosinus_gama_eye2 = df['gaze_0_y'][i-1] / magnitude_eye2

            result1 = math.acos(cosinus_gama_eye1) #radian - inverse cosine
            result2 = math.acos(cosinus_gama_eye2) #radian - inverse cosine

            angle_eye_1 = math.degrees(result1) #degree
            angle_eye_2 = math.degrees(result2)

            if angle_eye_1 - angle_eye_2 < 0:
                facial_features2['B10_looking_down']['value'] = 1;
                B10_looking_down_Frames = B10_looking_down_Frames +1
            else:
                facial_features2['B10_looking_down']['value'] = 0;  


        # B11 lips down *******************      

        if df['AU15_r'][i]>=1 or df['AU20_r'][i]>=1:
            facial_features2['B11_lips_down']['value'] = 1
            facial_features2['B11_lips_down']['distribution']['AU15_r'] = df['AU15_r'][i]
            facial_features2['B11_lips_down']['distribution']['AU20_r'] = df['AU20_r'][i]
            B11_lips_down_intensity = B11_lips_down_intensity + ((df['AU15_r'][i] + df['AU20_r'][i])/2)
            B11_lips_down_Frames = B11_lips_down_Frames +1
        else:
            facial_features2['B11_lips_down']['value'] = 0
            facial_features2['B11_lips_down']['distribution']['AU15_r'] = df['AU15_r'][i]
            facial_features2['B11_lips_down']['distribution']['AU20_r'] = df['AU20_r'][i]



        # B12 Head facing away *******************         

        if abs(df['pose_Ry'][i])>0.3:
            facial_features2['B12_head_away_updown']['value'] = 1
            B12_head_away_updown_Frames = B12_head_away_updown_Frames +1
        else:
            facial_features2['B12_head_away_updown']['value'] = 0

        if abs(df['pose_Rx'][i])>0.3:
            facial_features2['B12_head_away_leftright']['value'] = 1
            B12_head_away_leftright_Frames = B12_head_away_leftright_Frames +1
        else:
            facial_features2['B12_head_away_leftright']['value'] = 0

        if abs(df['pose_Rz'][i])>0.3:
            facial_features2['B12_head_away_zdirection']['value'] = 1
            B12_head_away_zdirection_Frames = B12_head_away_zdirection_Frames +1
        else:
            facial_features2['B12_head_away_zdirection']['value'] = 0



        # B13 Head down *******************         

        if df['pose_Rx'][i] < 0:
            facial_features2['B13_head_down']['value'] = 1
            B13_head_down_Frames = B13_head_down_Frames +1
        else:
            facial_features2['B13_head_down']['value'] = 0

        # A8_smiling *******************         

        if df['AU06_r'][i]>=1 and df['AU12_r'][i]>=1:
            facial_features2['A8_smiling']['value'] = 1
            facial_features2['A8_smiling']['distribution']['AU06_r'] = df['AU06_r'][i]
            facial_features2['A8_smiling']['distribution']['AU12_r'] = df['AU12_r'][i]
            A8_smiling_intensity = A8_smiling_intensity + ((df['AU06_r'][i]+df['AU12_r'][i])/2)
            A8_smiling_Frames = A8_smiling_Frames +1            

        else:
            facial_features2['A8_smiling']['value'] = 0
            facial_features2['A8_smiling']['distribution']['AU06_r'] = df['AU06_r'][i]
            facial_features2['A8_smiling']['distribution']['AU12_r'] = df['AU12_r'][i]

            A8_smiling_all_values.append(facial_features2['A8_smiling']['value'])

        # A7_squeezinglip_ *******************       

        if df['AU23_r'][i]==1 :
            facial_features2['A7_squeezinglip']['value'] = 1
            facial_features2['A7_squeezinglip']['distribution']['AU23_r'] = df['AU23_r'][i]
            A7_squeezinglip_intensity = A7_squeezinglip_intensity + df['AU23_r'][i]
            A7_squeezinglip_Frames = A7_squeezinglip_Frames +1
        else:
            facial_features2['A7_squeezinglip']['value'] = 0

        # A6_eyebrows *******************         

        if df['AU01_r'][i]>=1 or df['AU02_r'][i]>=1 or df['AU04_r'][i]>=1:
            facial_features2['A6_eyebrows']['value'] = 1
            facial_features2['A6_eyebrows']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['A6_eyebrows']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['A6_eyebrows']['distribution']['AU04_r'] = df['AU04_r'][i]
            A6_eyebrows_intensity = A6_eyebrows_intensity + (df['AU01_r'][i]+df['AU02_r'][i]+df['AU04_r'][i])/3
            A6_eyebrows_Frames = A6_eyebrows_Frames +1
        else:
            facial_features2['A6_eyebrows']['value'] = 0
            facial_features2['A6_eyebrows']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['A6_eyebrows']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['A6_eyebrows']['distribution']['AU04_r'] = df['AU04_r'][i]

        # A5_frowning *******************         

        if df['AU04_r'][i]>=1:
            facial_features2['A5_frowning']['value'] = 1
            facial_features2['A5_frowning']['distribution']['AU04_r'] = df['AU04_r'][i]
            A5_frowning_intensity = A5_frowning_intensity + df['AU04_r'][i]
            A5_frowning_Frames = A5_frowning_Frames +1
        else:
            facial_features2['A5_frowning']['value'] = 0
            facial_features2['A5_frowning']['distribution']['AU04_r'] = df['AU04_r'][i]

             # ***************************************EMOTIONS ************************************************ 
            # ***************************************EMOTIONS ************************************************ 
            # ***************************************EMOTIONS ************************************************ 

        intensity = df['AU01_r'][i] + df['AU02_r'][i] + df['AU04_r'][i] + df['AU05_r'][i] + df['AU06_r'][i] + df['AU07_r'][i] + df['AU09_r'][i]+ df['AU10_r'][i] + df['AU12_r'][i] + df['AU14_r'][i] + df['AU15_r'][i] + df['AU17_r'][i] + df['AU20_r'][i] + df['AU23_r'][i]+ df['AU25_r'][i] + df['AU26_r'][i] + df['AU28_c'][i]+ df['AU45_r'][i]

        Intensities = intensity;

        if i == 0:
            minima_intensity = intensity

        if minima_intensity > intensity:
            minima_intensity = intensity

            # neutral *******************         

        if Intensities <= minima_intensity:
            facial_features2['B15_1_emotions_neutral']['value'] = 1
            facial_features2['B15_1_emotions_neutral']['intenstiy'] = Intensities       
            facial_features2['B15_1_emotions_neutral']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU06_r'] = df['AU06_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU07_r'] = df['AU07_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU10_r'] = df['AU10_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU12_r'] = df['AU12_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU14_r'] = df['AU14_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU15_r'] = df['AU15_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU17_r'] = df['AU17_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU20_r'] = df['AU20_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU23_r'] = df['AU23_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU25_r'] = df['AU25_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU26_r'] = df['AU26_r'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU28_c'] = df['AU28_c'][i]
            facial_features2['B15_1_emotions_neutral']['distribution']['AU45_r'] = df['AU45_r'][i]
            NeutralFrames = NeutralFrames + 1;

            # angry *******************   
            # ANGER 4+5+7+10+22+23+25/26 (NA) || 4+5+7+10+23+25/26 || 4+5+7+17+23/24 || 4+5+7+23/24 || 4+5/7 ||

        # ANGER 4+5+7+10+23+25/26 
        if df['AU04_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0 and df['AU07_r'][i] >= 1.0 and df['AU10_r'][i] >= 1.0 and df['AU23_r'][i] and (df['AU25_r'][i] >= 1.0 or df['AU26_r'][i] >= 1.0):
            facial_features2['B15_2_emotions_angry']['value'] = 1
            facial_features2['B15_2_emotions_angry']['intenstiy'] = (df['AU04_r'][i]+df['AU05_r'][i]+df['AU07_r'][i]+df['AU10_r'][i]+df['AU23_r'][i]+df['AU25_r'][i]+df['AU26_r'][i])
            facial_features2['B15_2_emotions_angry']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU07_r'] = df['AU07_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU10_r'] = df['AU10_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU07_r'] = df['AU07_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU23_r'] = df['AU23_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU25_r'] = df['AU25_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU26_r'] = df['AU26_r'][i]
            AngryFrames = AngryFrames + 1

            B15_2_emotions_angry_intenstiy = B15_2_emotions_angry_intenstiy + (facial_features2['B15_2_emotions_angry']['intenstiy']/8)

        # ANGER 4+5+7+17+23/24     
        elif df['AU04_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0 and df['AU07_r'][i] >= 1.0 and df['AU17_r'][i] >= 1.0 and df['AU23_r'][i]: 
            facial_features2['B15_2_emotions_angry']['value'] = 1
            facial_features2['B15_2_emotions_angry']['intenstiy'] = (df['AU04_r'][i]+df['AU05_r'][i]+df['AU07_r'][i]+df['AU17_r'][i]+df['AU23_r'][i])
            facial_features2['B15_2_emotions_angry']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU07_r'] = df['AU07_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU17_r'] = df['AU17_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU23_r'] = df['AU23_r'][i]
            AngryFrames = AngryFrames + 1

            B15_2_emotions_angry_intenstiy = B15_2_emotions_angry_intenstiy + (facial_features2['B15_2_emotions_angry']['intenstiy']/5)

        # ANGER 4+5+7+23/24     
        elif df['AU04_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0 and df['AU07_r'][i] >= 1.0 and df['AU23_r'][i]:
            facial_features2['B15_2_emotions_angry']['value'] = 1
            facial_features2['B15_2_emotions_angry']['intenstiy'] = (df['AU04_r'][i]+df['AU05_r'][i]+df['AU07_r'][i]+df['AU23_r'][i])
            facial_features2['B15_2_emotions_angry']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU07_r'] = df['AU07_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU23_r'] = df['AU23_r'][i]
            AngryFrames = AngryFrames + 1

            B15_2_emotions_angry_intenstiy = B15_2_emotions_angry_intenstiy + (facial_features2['B15_2_emotions_angry']['intenstiy']/4)


        # ANGER 4+5/7    
        elif df['AU04_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0 and df['AU07_r'][i] >= 1.0: 
            facial_features2['B15_2_emotions_angry']['value'] = 1
            facial_features2['B15_2_emotions_angry']['intenstiy'] = (df['AU04_r'][i]+df['AU05_r'][i]+df['AU07_r'][i])
            facial_features2['B15_2_emotions_angry']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_2_emotions_angry']['distribution']['AU07_r'] = df['AU07_r'][i]
            AngryFrames = AngryFrames + 1  

            B15_2_emotions_angry_intenstiy = B15_2_emotions_angry_intenstiy + (facial_features2['B15_2_emotions_angry']['intenstiy']/3)


        # DISGUST ******************* 
        # DISGUST 9/10+17 || 9/10+16+25/26 (NA)  || 9/10         
        # DISGUST 9/10+17     

        if (df['AU09_r'][i] >= 1.0 or df['AU10_r'][i] >= 1.0) and df['AU17_r'][i] >= 1.0: 
            facial_features2['B15_3_emotions_disgust']['value'] = 1
            facial_features2['B15_3_emotions_disgust']['intenstiy'] = (df['AU09_r'][i]+df['AU10_r'][i]+df['AU17_r'][i])
            facial_features2['B15_3_emotions_disgust']['distribution']['AU09_r'] = df['AU09_r'][i]
            facial_features2['B15_3_emotions_disgust']['distribution']['AU10_r'] = df['AU10_r'][i]
            facial_features2['B15_3_emotions_disgust']['distribution']['AU17_r'] = df['AU17_r'][i]
            DisgustFrames = DisgustFrames + 1

            B15_3_emotions_disgust_intenstiy = B15_3_emotions_disgust_intenstiy + (facial_features2['B15_3_emotions_disgust']['intenstiy']/3)

        # DISGUST 9/10
        elif df['AU09_r'][i] >= 1.0 or df['AU10_r'][i] >= 1.0: 
            facial_features2['B15_3_emotions_disgust']['value'] = 1
            facial_features2['B15_3_emotions_disgust']['intenstiy'] = (df['AU09_r'][i]+df['AU10_r'][i])
            facial_features2['B15_3_emotions_disgust']['distribution']['AU09_r'] = df['AU09_r'][i]
            facial_features2['B15_3_emotions_disgust']['distribution']['AU10_r'] = df['AU10_r'][i]
            DisgustFrames = DisgustFrames + 1

            B15_3_emotions_disgust_intenstiy = B15_3_emotions_disgust_intenstiy + (facial_features2['B15_3_emotions_disgust']['intenstiy']/2)

        # FEAR 1+2+4 || 1+2+4+5+20+25/26/27 || 1+2+4+5+25/26/27  ||  1+2+4+5  ||  1+2+5+25/26/27  || 5+20+25/26/27  ||  5+20 || 20       
        # FEAR 1+2+4   
        if df['AU01_r'][i] >= 1.0 and df['AU02_r'][i] >= 1.0 and df['AU04_r'][i] >= 1.0: 
            facial_features2['B15_4_emotions_fear']['value'] = 1
            facial_features2['B15_4_emotions_fear']['intenstiy'] = (df['AU01_r'][i]+df['AU02_r'][i]+df['AU04_r'][i])
            facial_features2['B15_4_emotions_fear']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU04_r'] = df['AU04_r'][i]
            FearFrames = FearFrames + 1

            B15_4_emotions_fear_intenstiy = B15_4_emotions_fear_intenstiy + (facial_features2['B15_4_emotions_fear']['intenstiy']/3)

        # FEAR 1+2+4+5+20+25/26/27
        elif df['AU01_r'][i] >= 1.0 and df['AU02_r'][i] >= 1.0 and df['AU04_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0 and df['AU20_r'][i] >= 1.0 and (df['AU25_r'][i] >= 1.0 or df['AU26_r'][i] >= 1.0): 
            facial_features2['B15_4_emotions_fear']['value'] = 1
            facial_features2['B15_4_emotions_fear']['intenstiy'] = (df['AU01_r'][i]+df['AU02_r'][i]+df['AU04_r'][i]+df['AU05_r'][i]+df['AU20_r'][i]+df['AU25_r'][i]+df['AU26_r'][i])
            facial_features2['B15_4_emotions_fear']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU20_r'] = df['AU20_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU25_r'] = df['AU25_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU26_r'] = df['AU26_r'][i]
            FearFrames = FearFrames + 1

            B15_4_emotions_fear_intenstiy = B15_4_emotions_fear_intenstiy + (facial_features2['B15_4_emotions_fear']['intenstiy']/7)

        # FEAR 1+2+4+5+25/26/27
        elif df['AU01_r'][i] >= 1.0 and df['AU02_r'][i] >= 1.0 and df['AU04_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0 and (df['AU25_r'][i] >= 1.0 or df['AU26_r'][i] >= 1.0): 
            facial_features2['B15_4_emotions_fear']['value'] = 1
            facial_features2['B15_4_emotions_fear']['intenstiy'] = (df['AU01_r'][i]+df['AU02_r'][i]+df['AU04_r'][i]+df['AU05_r'][i]+df['AU25_r'][i]+df['AU26_r'][i])
            facial_features2['B15_4_emotions_fear']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU25_r'] = df['AU25_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU26_r'] = df['AU26_r'][i]
            FearFrames = FearFrames + 1

            B15_4_emotions_fear_intenstiy = B15_4_emotions_fear_intenstiy + (facial_features2['B15_4_emotions_fear']['intenstiy']/6)

        # FEAR 1+2+4+5
        elif df['AU01_r'][i] >= 1.0 and df['AU02_r'][i] >= 1.0 and df['AU04_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0: 
            facial_features2['B15_4_emotions_fear']['value'] = 1
            facial_features2['B15_4_emotions_fear']['intenstiy'] = (df['AU01_r'][i]+df['AU02_r'][i]+df['AU04_r'][i]+df['AU05_r'][i])
            facial_features2['B15_4_emotions_fear']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU05_r'] = df['AU05_r'][i]
            FearFrames = FearFrames + 1

            B15_4_emotions_fear_intenstiy = B15_4_emotions_fear_intenstiy + (facial_features2['B15_4_emotions_fear']['intenstiy']/4)

        # FEAR 1+2+5+25/26/27
        elif df['AU01_r'][i] >= 1.0 and df['AU02_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0 and (df['AU25_r'][i] >= 1.0 or df['AU26_r'][i] >= 1.0): 
            facial_features2['B15_4_emotions_fear']['value'] = 1
            facial_features2['B15_4_emotions_fear']['intenstiy'] = (df['AU01_r'][i]+df['AU02_r'][i]+df['AU05_r'][i]+df['AU25_r'][i]+df['AU26_r'][i])
            facial_features2['B15_4_emotions_fear']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU25_r'] = df['AU25_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU26_r'] = df['AU26_r'][i]
            FearFrames = FearFrames + 1

            B15_4_emotions_fear_intenstiy = B15_4_emotions_fear_intenstiy + (facial_features2['B15_4_emotions_fear']['intenstiy']/5)

        # FEAR 5+20+25/26/27
        elif df['AU05_r'][i] >= 1.0 and df['AU20_r'][i] >= 1.0 and (df['AU25_r'][i] >= 1.0 or df['AU26_r'][i] >= 1.0): 
            facial_features2['B15_4_emotions_fear']['value'] = 1
            facial_features2['B15_4_emotions_fear']['intenstiy'] = (df['AU05_r'][i]+df['AU20_r'][i]+df['AU25_r'][i]+df['AU26_r'][i])
            facial_features2['B15_4_emotions_fear']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU20_r'] = df['AU20_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU25_r'] = df['AU25_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU26_r'] = df['AU26_r'][i]
            FearFrames = FearFrames + 1

            B15_4_emotions_fear_intenstiy = B15_4_emotions_fear_intenstiy + (facial_features2['B15_4_emotions_fear']['intenstiy']/4)

        # FEAR 5+20
        elif df['AU05_r'][i] >= 1.0 and df['AU20_r'][i] >= 1.0: 
            facial_features2['B15_4_emotions_fear']['value'] = 1
            facial_features2['B15_4_emotions_fear']['intenstiy'] = (df['AU05_r'][i]+df['AU20_r'][i])
            facial_features2['B15_4_emotions_fear']['distribution'] = {}
            facial_features2['B15_4_emotions_fear']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_4_emotions_fear']['distribution']['AU20_r'] = df['AU20_r'][i]
            FearFrames = FearFrames + 1

            B15_4_emotions_fear_intenstiy = B15_4_emotions_fear_intenstiy + (facial_features2['B15_4_emotions_fear']['intenstiy']/2)

        # FEAR 20
        elif df['AU20_r'][i] >= 1.0:         
            facial_features2['B15_4_emotions_fear']['value'] = 1
            facial_features2['B15_4_emotions_fear']['intenstiy'] = (df['AU20_r'][i])
            facial_features2['B15_4_emotions_fear']['distribution']['AU20_r'] = df['AU20_r'][i]
            FearFrames = FearFrames + 1

            B15_4_emotions_fear_intenstiy = B15_4_emotions_fear_intenstiy + facial_features2['B15_4_emotions_fear']['intenstiy']

        # HAPPY **************
        # HAPPY 12 || 6+12 
        if df['AU12_r'][i] >= 1.0: 
            facial_features2['B15_5_emotions_happy']['value'] = 1
            facial_features2['B15_5_emotions_happy']['intenstiy'] = (df['AU12_r'][i])
            facial_features2['B15_5_emotions_happy']['distribution']['AU12_r'] = df['AU12_r'][i]
            HappyFrames = HappyFrames + 1

            B15_5_emotions_happy_intenstiy = B15_5_emotions_happy_intenstiy + (facial_features2['B15_5_emotions_happy']['intenstiy'])

        elif df['AU06_r'][i] >= 1.0 and df['AU12_r'][i] >= 1.0: 
            facial_features2['B15_5_emotions_happy']['value'] = 1
            facial_features2['B15_5_emotions_happy']['intenstiy'] = (df['AU06_r'][i]+df['AU12_r'][i])
            facial_features2['B15_5_emotions_happy']['distribution']['AU06_r'] = df['AU06_r'][i]
            facial_features2['B15_5_emotions_happy']['distribution']['AU12_r'] = df['AU12_r'][i]
            HappyFrames = HappyFrames + 1

            B15_5_emotions_happy_intenstiy = B15_5_emotions_happy_intenstiy + (facial_features2['B15_5_emotions_happy']['intenstiy']/2)

        # SADNESS 1+4 || 1+4+11/15 || 1+4+15+17 || 6+15  || 11+17 (NA) || 1  
        # SADNESS 1+4
        if df['AU01_r'][i] >= 1.0 and df['AU04_r'][i] >= 1.0: 
            facial_features2['B15_6_emotions_sadness']['value'] = 1
            facial_features2['B15_6_emotions_sadness']['intenstiy'] = (df['AU01_r'][i]+df['AU04_r'][i])
            facial_features2['B15_6_emotions_sadness']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_6_emotions_sadness']['distribution']['AU04_r'] = df['AU04_r'][i]
            SadnessFrames = SadnessFrames + 1

            B15_6_emotions_sadness_intenstiy = B15_6_emotions_sadness_intenstiy + (facial_features2['B15_6_emotions_sadness']['intenstiy']/2)

        # SADNESS  1+4+11/15    
        elif df['AU01_r'][i] >= 1.0 and df['AU04_r'][i] >= 1.0 and df['AU15_r'][i] >= 1.0: 
            facial_features2['B15_6_emotions_sadness']['value'] = 1
            facial_features2['B15_6_emotions_sadness']['intenstiy'] = (df['AU01_r'][i]+df['AU04_r'][i]+df['AU15_r'][i])
            facial_features2['B15_6_emotions_sadness']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_6_emotions_sadness']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_6_emotions_sadness']['distribution']['AU15_r'] = df['AU15_r'][i]
            SadnessFrames = SadnessFrames + 1

            B15_6_emotions_sadness_intenstiy = B15_6_emotions_sadness_intenstiy + (facial_features2['B15_6_emotions_sadness']['intenstiy']/3)

        # SADNESS  1+4+15+17    
        elif df['AU01_r'][i] >= 1.0 and df['AU04_r'][i] >= 1.0 and df['AU15_r'][i] >= 1.0 and df['AU17_r'][i] >= 1.0: 
            facial_features2['B15_6_emotions_sadness']['value'] = 1
            facial_features2['B15_6_emotions_sadness']['intenstiy'] = (df['AU01_r'][i]+df['AU04_r'][i]+df['AU15_r'][i]+df['AU17_r'][i])
            facial_features2['B15_6_emotions_sadness']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_6_emotions_sadness']['distribution']['AU04_r'] = df['AU04_r'][i]
            facial_features2['B15_6_emotions_sadness']['distribution']['AU15_r'] = df['AU15_r'][i]
            facial_features2['B15_6_emotions_sadness']['distribution']['AU17_r'] = df['AU17_r'][i]
            SadnessFrames = SadnessFrames + 1

            B15_6_emotions_sadness_intenstiy = B15_6_emotions_sadness_intenstiy + (facial_features2['B15_6_emotions_sadness']['intenstiy']/4)

        # SADNESS 6+15
        elif df['AU06_r'][i] >= 1.0 and df['AU15_r'][i] >= 1.0: 
            facial_features2['B15_6_emotions_sadness']['value'] = 1
            facial_features2['B15_6_emotions_sadness']['intenstiy'] = (df['AU06_r'][i]+df['AU15_r'][i])
            facial_features2['B15_6_emotions_sadness']['distribution']['AU06_r'] = df['AU06_r'][i]
            facial_features2['B15_6_emotions_sadness']['distribution']['AU15_r'] = df['AU15_r'][i]
            SadnessFrames = SadnessFrames + 1

            B15_6_emotions_sadness_intenstiy = B15_6_emotions_sadness_intenstiy + (facial_features2['B15_6_emotions_sadness']['intenstiy']/2)

        # SADNESS 1
        elif df['AU01_r'][i] >= 1.0: 
            facial_features2['B15_6_emotions_sadness']['value'] = 1
            facial_features2['B15_6_emotions_sadness']['intenstiy'] = (df['AU01_r'][i])
            facial_features2['B15_6_emotions_sadness']['distribution']['AU01_r'] = df['AU01_r'][i]
            SadnessFrames = SadnessFrames + 1

            B15_6_emotions_sadness_intenstiy = B15_6_emotions_sadness_intenstiy + (facial_features2['B15_6_emotions_sadness']['intenstiy'])

        # SURPRISE 1+2+5+26/27 || 1+2+5 || 1+2+26/27 || 5+26/27 
        # SURPRISE 1+2+5+26/27
        if df['AU01_r'][i] >= 1.0 and df['AU02_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0 and df['AU26_r'][i] >= 1.0: 
            facial_features2['B15_7_emotions_surprise']['value'] = 1
            facial_features2['B15_7_emotions_surprise']['intenstiy'] = (df['AU01_r'][i]+df['AU02_r'][i]+df['AU05_r'][i]+df['AU26_r'][i])
            facial_features2['B15_7_emotions_surprise']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_7_emotions_surprise']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_7_emotions_surprise']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_7_emotions_surprise']['distribution']['AU26_r'] = df['AU26_r'][i]
            SurpriseFrames = SurpriseFrames + 1

            B15_7_emotions_surprise_intenstiy = B15_7_emotions_surprise_intenstiy + (facial_features2['B15_7_emotions_surprise']['intenstiy']/4)

        # SURPRISE 1+2+5
        elif df['AU01_r'][i] >= 1.0 and df['AU02_r'][i] >= 1.0 and df['AU05_r'][i] >= 1.0: 
            facial_features2['B15_7_emotions_surprise']['value'] = 1
            facial_features2['B15_7_emotions_surprise']['intenstiy'] = (df['AU01_r'][i]+df['AU02_r'][i]+df['AU05_r'][i])
            facial_features2['B15_7_emotions_surprise']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_7_emotions_surprise']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_7_emotions_surprise']['distribution']['AU05_r'] = df['AU05_r'][i]
            SurpriseFrames = SurpriseFrames + 1

            B15_7_emotions_surprise_intenstiy = B15_7_emotions_surprise_intenstiy + (facial_features2['B15_7_emotions_surprise']['intenstiy']/3)

        # SURPRISE 1+2+26/27
        elif df['AU01_r'][i] >= 1.0 and df['AU02_r'][i] >= 1.0 and df['AU26_r'][i] >= 1.0: 
            facial_features2['B15_7_emotions_surprise']['value'] = 1
            facial_features2['B15_7_emotions_surprise']['intenstiy'] = (df['AU01_r'][i]+df['AU02_r'][i]+df['AU26_r'][i])
            facial_features2['B15_7_emotions_surprise']['distribution']['AU01_r'] = df['AU01_r'][i]
            facial_features2['B15_7_emotions_surprise']['distribution']['AU02_r'] = df['AU02_r'][i]
            facial_features2['B15_7_emotions_surprise']['distribution']['AU26_r'] = df['AU26_r'][i]
            SurpriseFrames = SurpriseFrames + 1

            B15_7_emotions_surprise_intenstiy = B15_7_emotions_surprise_intenstiy + (facial_features2['B15_7_emotions_surprise']['intenstiy']/3)

        # SURPRISE 5+26/27
        elif df['AU05_r'][i] >= 1.0 and df['AU26_r'][i] >= 1.0: 
            facial_features2['B15_7_emotions_surprise']['value'] = 1
            facial_features2['B15_7_emotions_surprise']['intenstiy'] = (df['AU05_r'][i]+df['AU26_r'][i])
            facial_features2['B15_7_emotions_surprise']['distribution']['AU05_r'] = df['AU05_r'][i]
            facial_features2['B15_7_emotions_surprise']['distribution']['AU26_r'] = df['AU26_r'][i]
            SurpriseFrames = SurpriseFrames + 1    

            B15_7_emotions_surprise_intenstiy = B15_7_emotions_surprise_intenstiy + (facial_features2['B15_7_emotions_surprise']['intenstiy']/2)   


        #frames['frames'][i] = facial_features2                                                                  


    B3_3_Head_Amplitude_Frames_Percentage.append(float("{0:.3f}".format(100*(B3_3_Head_Amplitude_Frames/total_frame))))
    B9_face_moving_Frames_Percentage.append(float("{0:.3f}".format(100*(B9_face_moving_Frames/total_frame))))
    B9_mouth_moving_Frames_Percentage.append(float("{0:.3f}".format(100*(B9_mouth_moving_Frames/total_frame))))
    B10_looking_down_Frames_Percentage.append(float("{0:.3f}".format(100*(B10_looking_down_Frames / total_frame))))
    B11_lips_down_Frames_Percentage.append(float("{0:.3f}".format(100*(B11_lips_down_Frames / total_frame))))
    B12_head_away_updown_Frames_Percentage.append(float("{0:.3f}".format(100*(B12_head_away_updown_Frames / total_frame))))
    B12_head_away_leftright_Frames_Percentage.append(float("{0:.3f}".format(100*(B12_head_away_leftright_Frames / total_frame))))
    B12_head_away_zdirection_Frames_Percentage.append(float("{0:.3f}".format(100*(B12_head_away_zdirection_Frames / total_frame))))

    B13_head_down_Frames_Percentage.append(float("{0:.3f}".format(100*(B13_head_down_Frames / total_frame))))
    B14_Rotational_Energy_Frames_small_Percentage.append(float("{0:.3f}".format(100*(B14_Rotational_Energy_Frames_small  / total_frame))))
    B14_Rotational_Energy_Frames_large_Percentage.append(float("{0:.3f}".format(100*(B14_Rotational_Energy_Frames_large  / total_frame))))
    B14_Rotational_Energy_Frames_higher_Percentage.append(float("{0:.3f}".format(100*(B14_Rotational_Energy_Frames_higher  / total_frame))))

    A5_frowning_Frames_Percentage.append(float("{0:.3f}".format(100*(A5_frowning_Frames / total_frame))))
    A6_eyebrows_Frames_Percentage.append(float("{0:.3f}".format(100*(A6_eyebrows_Frames / total_frame))))
    A7_squeezinglip_Frames_Percentage.append(float("{0:.3f}".format(100*(A7_squeezinglip_Frames / total_frame))))
    A8_smiling_Frames_Percentage.append(float("{0:.3f}".format(100*(A8_smiling_Frames / total_frame))))

    NeutralFrames_percentage.append((float("{0:.2f}".format(100*(NeutralFrames/total_frame)))))
    AngryFrames_percentage.append((float("{0:.2f}".format(100*(AngryFrames/total_frame)))))
    DisgustFrames_percentage.append((float("{0:.2f}".format(100*(DisgustFrames/total_frame)))))
    FearFrames_percentage.append((float("{0:.2f}".format(100*(FearFrames/total_frame)))))
    HappyFrames_percentage.append((float("{0:.2f}".format(100*(HappyFrames/total_frame)))))
    SadnessFrames_percentage.append((float("{0:.2f}".format(100*(SadnessFrames/total_frame)))))
    SurpriseFrames_percentage.append((float("{0:.2f}".format(100*(SurpriseFrames/total_frame)))))



    if B9_mouth_moving_Frames == 0:
        B9_mouth_moving_intensity_video_avg.append(0)
    else:
        B9_mouth_moving_intensity_video_avg.append(B9_mouth_moving_intensity / B9_mouth_moving_Frames)

    if B11_lips_down_Frames == 0:
        B11_lips_down_intensity_video_avg.append(0)
    else:
        B11_lips_down_intensity_video_avg.append(B11_lips_down_intensity / B11_lips_down_Frames)

    if A5_frowning_Frames == 0:
        A5_frowning_video_avg.append(0)
    else:
        A5_frowning_video_avg.append(A5_frowning_intensity / A5_frowning_Frames)

    if A6_eyebrows_Frames == 0:
        A6_eyebrows_video_avg.append(0)
    else:
        A6_eyebrows_video_avg.append(A6_eyebrows_intensity / A6_eyebrows_Frames)

    if A7_squeezinglip_Frames == 0:
        A7_squeezinglip_video_avg.append(0)
    else:
        A7_squeezinglip_video_avg.append(A7_squeezinglip_intensity / A7_squeezinglip_Frames)

    if A8_smiling_Frames == 0:
        A8_smiling_video_avg.append(0)
    else:
        A8_smiling_video_avg.append(A8_smiling_intensity / A8_smiling_Frames)

    if AngryFrames == 0:
        B15_2_emotions_angry_intenstiy_video_avg.append(0)
    else:
        B15_2_emotions_angry_intenstiy_video_avg.append(B15_2_emotions_angry_intenstiy / AngryFrames)

    if DisgustFrames == 0:
        B15_3_emotions_disgust_intenstiy_video_avg.append(0)
    else:   
        B15_3_emotions_disgust_intenstiy_video_avg.append(B15_3_emotions_disgust_intenstiy / DisgustFrames)

    if FearFrames == 0:
        B15_4_emotions_fear_intenstiy_video_avg.append(0)
    else:     
        B15_4_emotions_fear_intenstiy_video_avg.append(B15_4_emotions_fear_intenstiy / FearFrames)

    if HappyFrames == 0:
        B15_5_emotions_happy_intenstiy_video_avg.append(0)
    else:
        B15_5_emotions_happy_intenstiy_video_avg.append(B15_5_emotions_happy_intenstiy / HappyFrames)

    if SadnessFrames == 0:
        B15_6_emotions_sadness_intenstiy_video_avg.append(0)
    else:
        B15_6_emotions_sadness_intenstiy_video_avg.append(B15_6_emotions_sadness_intenstiy / SadnessFrames)

    if SurpriseFrames == 0:
        B15_7_emotions_surprise_intenstiy_video_avg.append(0)
    else:
        B15_7_emotions_surprise_intenstiy_video_avg.append(B15_7_emotions_surprise_intenstiy / SurpriseFrames)  


    emotion_variability = []
    
    total_emotion_variability = 0

    emotion_variability.append(B15_2_emotions_angry_intenstiy_video_avg)
    emotion_variability.append(B15_3_emotions_disgust_intenstiy_video_avg)
    emotion_variability.append(B15_4_emotions_fear_intenstiy_video_avg)
    emotion_variability.append(B15_5_emotions_happy_intenstiy_video_avg)
    emotion_variability.append(B15_6_emotions_sadness_intenstiy_video_avg)
    emotion_variability.append(B15_7_emotions_surprise_intenstiy_video_avg)

    len_var = len(emotion_variability)

    for i in range(len_var):        
        value_emotion = emotion_variability[i][0]
        if value_emotion > 1.0:
            total_emotion_variability = total_emotion_variability +1

    video_emotion_variability.append(total_emotion_variability)
    
    
    df2 = pd.DataFrame([[float("{0:.2f}".format(sum(AngryFrames_percentage)))], 
			 [float("{0:.2f}".format(sum(DisgustFrames_percentage)))],
			 [float("{0:.2f}".format(sum(FearFrames_percentage)))], 
			 [float("{0:.2f}".format(sum(HappyFrames_percentage)))], 
             [float("{0:.2f}".format(sum(SadnessFrames_percentage)))], 
             [float("{0:.2f}".format(sum(SurpriseFrames_percentage)))],
             [float("{0:.2f}".format(sum(B15_2_emotions_angry_intenstiy_video_avg)))], 
			 [float("{0:.2f}".format(sum(B15_3_emotions_disgust_intenstiy_video_avg)))], 
             [float("{0:.2f}".format(sum(B15_4_emotions_fear_intenstiy_video_avg)))], 
             [float("{0:.2f}".format(sum(B15_5_emotions_happy_intenstiy_video_avg)))],
             [float("{0:.2f}".format(sum(B15_6_emotions_sadness_intenstiy_video_avg)))], 
			 [float("{0:.2f}".format(sum(B15_7_emotions_surprise_intenstiy_video_avg)))],
             [float("{0:.2f}".format(sum(NeutralFrames_percentage)))],
             [max(video_emotion_variability)],
             [float("{0:.2f}".format(sum(A6_eyebrows_video_avg)))],             
			 [float("{0:.2f}".format(sum(A6_eyebrows_Frames_Percentage)))],
             [float("{0:.2f}".format(sum(B10_looking_down_Frames_Percentage)))],       
             [float("{0:.2f}".format(sum(A5_frowning_video_avg)))], 
			 [float("{0:.2f}".format(sum(A5_frowning_Frames_Percentage)))],     
             [float("{0:.2f}".format(sum(B3_3_Head_Amplitude_Frames_Percentage)))], 
			 [float("{0:.2f}".format(sum(B9_face_moving_Frames_Percentage)))],
             [float("{0:.2f}".format(sum(B12_head_away_updown_Frames_Percentage)))], 
             [float("{0:.2f}".format(sum(B12_head_away_leftright_Frames_Percentage)))],   
             [float("{0:.2f}".format(sum(B12_head_away_zdirection_Frames_Percentage)))], 
             [float("{0:.2f}".format(sum(B13_head_down_Frames_Percentage)))],
             [float("{0:.2f}".format(sum(B14_Rotational_Energy_Frames_small_Percentage)))],
             [float("{0:.2f}".format(sum(B14_Rotational_Energy_Frames_large_Percentage)))], 
			 [float("{0:.2f}".format(sum(B14_Rotational_Energy_Frames_higher_Percentage)))],
             [float("{0:.2f}".format(sum(B9_mouth_moving_Frames_Percentage)))],
			 [float("{0:.2f}".format(sum(B9_mouth_moving_intensity_video_avg)))],
             [float("{0:.2f}".format(sum(B11_lips_down_Frames_Percentage)))],
			 [float("{0:.2f}".format(sum(B11_lips_down_intensity_video_avg)))], 
             [float("{0:.2f}".format(sum(A7_squeezinglip_video_avg)))],
             [float("{0:.2f}".format(sum(A7_squeezinglip_Frames_Percentage)))],
			 [float("{0:.2f}".format(sum(A8_smiling_video_avg)))],
             [float("{0:.2f}".format(sum(A8_smiling_Frames_Percentage)))]],columns=['Visual'])
    
    # Save results to a separate CSV file
    output_file = f"/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/processed/{video_name}/output_{csv_file_path.split('/')[-1]}"  # Adjust output file naming as needed
    df2.to_csv(output_file, index=False)

def process_files_in_parallel(file_paths):
    with multiprocessing.Pool(processes) as pool:
        pool.map(feature_calculation, file_paths)

#file_paths = ["file1.csv", "file2.csv", "file3.csv"]  # Replace with your list of files

file_paths = [
    f"/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/processed/{video_name}/{video_name}_video_{i+1}.csv"
    for i in range(num_subvideos)
]
process_files_in_parallel(file_paths)


def calculate_row_stats(file_pattern):
  # Read all CSV files into a list of DataFrames
  dfs = [pd.read_csv(file, names=['Visual'], skiprows=1,) for file in glob.glob(file_pattern)]

  # Concatenate the DataFrames along the columns
  combined_df = pd.concat(dfs, axis=1)

  # Convert values to numeric
  combined_df = combined_df.astype(float)

  # Calculate statistics
  result = {}
  for i in range(36):
      if i == 13:
          result[f'Visual_{i+1}'] = combined_df.iloc[i].max()
      else:
          result[f'Visual_{i+1}'] = combined_df.iloc[i].mean()
  
  

  return result

file_pattern = '/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Docs/processed/'+video_name+'/output_'+video_name+'_video_*.csv'  # Replace with your actual file pattern
print("file_pattern: "+str(file_pattern))
result = calculate_row_stats(file_pattern)

#print(result)

names=['C1_emotion_AngryFrames_percentage',
				  'C1_emotion_DisgustFrames_percentage',
				  'C1_emotion_FearFrames_percentage',
				  'C1_emotion_HappyFrames_percentage',
                  'C1_emotion_SadnessFrames_percentage',
 				  'C1_emotion_SurpriseFrames_percentage',
                  'C2_emotion_AngryFrames_intensity',
				  'C2_emotion_DisgustFrames_intensity',
				  'C2_emotion_FearFrames_intensity',
				  'C2_emotion_HappyFrames_intensity',
                  'C2_emotion_SadnessFrames_intensity',
 				  'C2_emotion_SurpriseFrames_intensity',
                  'C3_NeutralFrames_percentage',                  
                  'C3_total_emotion_variability',
                  'C4_eyebrow_movement_intensity',
                  'C4_eyebrow_movement_percentage',
                  'C5_looking_down_Frames_Percentage',   
                  'C6_frowns_intensity',
				  'C6_frowns_percentage',  
                  'C7_head_moving_frames_percentage',
 				  'C7_face_moving_frames_percentage',
                  'C7_head_facing_away_updown_frames_percentage',
				  'C7_head_facing_away_leftright_frames_percentage',
                  'C7_head_facing_away_zdirection_frames_percentage',
                  'C7_head_down_frames_percentage',
                  'C8_rotational_energy_frames_low_percentage',				                    
                  'C8_rotational_energy_frames_moderate_percentage',
 				  'C8_rotational_energy_frames_high_percentage',                      
                  'C9_mouth_moving_frames_percentage',
                  'C9_mouth_moving_frames_intensity',                    
				  'C10_lips_press_frames_percentage',
				  'C10_lips_press_frames_intensity',
                  'C11_down_angled_mouth_corners_frames_intensity',
                  'C11_down_angled_mouth_corners_frames_percentage',				  
				  'C12_smiling_frames_intensity',
				  'C12_smiling_frames_percentage']

result_last = pd.DataFrame({'Visual': [result[f'Visual_{i+1}'] for i in range(36)]}, index=names)
 
#print(result_last)

output_file = "/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Output_Features/{}/{}_visual.json".format(video_name,video_name)
result_last.to_json(output_file)

print("---Facial Feature Extraction is ended in %s seconds ---" % (time.time() - start_time_visual))


