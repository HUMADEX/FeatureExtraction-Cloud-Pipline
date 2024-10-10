
##################################################################################
# ######################### SPEECH FEATURES EXTRACTION  ##################
# #################################################################################

import torch
torch.cuda.empty_cache()
import time

start_time = time.time()
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

import parselmouth
from parselmouth.praat import call, run_file

import scipy
from scipy.stats import binom
from scipy.stats import ks_2samp
from scipy.stats import ttest_ind
import os

import moviepy as mp
from moviepy.editor import *
from moviepy.video.io.VideoFileClip import VideoFileClip

#***********VIDEO lIBRARIES*********
#from IPython import get_ipython
#import pandas as pd, seaborn as sns
#sns.set_style('white')
#import matplotlib.pyplot as plt 
#from scipy.signal import find_peaks

import subprocess
import multiprocessing
from concurrent.futures import ThreadPoolExecutor



if torch.cuda.is_available():
    print("Running on GPU")
else:
    print("Running on CPU")

print("Current GPU device: "+str(torch.cuda.current_device()))  # Returns the current GPU device
print("Number of GPUs available: "+str(torch.cuda.device_count()))  # Returns the number of GPUs available
print("Name of the GPU: "+str(torch.cuda.get_device_name(0)))  # Returns the name of the GPU

video_name = sys.argv[1]
video_name = video_name.split(".")[0]  # Split by "." and take the first part

#video_name = 'user-id'

print('video_name:',video_name)

results_output_path = os.path.join('/path-to-output-folder-for-files/Output_Docs/', video_name)
print("results_output_path: "+results_output_path)
            
# Create the new folder
os.makedirs(results_output_path, exist_ok=True)

results_output_path2 = os.path.join('/path-to-output-folder-for-files/Output_Docs/processed/', video_name)
print("processed_results_output_path: "+results_output_path2)
            
# Create the new folder
os.makedirs(results_output_path2, exist_ok=True)



#***********Splitting Video into sub-videos*********

# Function to split video using ffmpeg
def split_video_ffmpeg(input_file, start_time, end_time, output_file):
    """Splits a video using ffmpeg between start_time and end_time, saving to output_file."""
    cmd = [
        'ffmpeg',
        '-y',  # Overwrite output file if it exists
        '-i', input_file,  # Input file
        '-ss', str(start_time),  # Start time after input file
        '-to', str(end_time),  # End time
        '-c:v', 'libx264',  # Re-encode video to H.264
        '-c:a', 'aac',  # Re-encode audio to AAC
        '-strict', 'experimental',  # For AAC codec support
        output_file
    ]
    subprocess.run(cmd, check=True)  # Raise an error if ffmpeg fails

# Main function to split video into sub-videos
def split_video(video_name, duration_video, filename_root_output, filename):
    """Main function to split the video into 30-second sub-videos."""
    tasks = []

    if duration_video < 30:
        split_video_ffmpeg(filename, 0, duration_video, f'{filename_root_output}{video_name}_video_1.mov')
        num_subvideos = 1
    else:
        num_subvideos = int(duration_video // 30)
        with ThreadPoolExecutor() as executor:
            for i in range(num_subvideos):
                start_time = i * 30
                end_time = start_time + 30
                output_file = f'{filename_root_output}{video_name}_video_{i + 1}.mov'
                tasks.append(executor.submit(split_video_ffmpeg, filename, start_time, end_time, output_file))

            # Handle remaining part of the video
            if duration_video - (num_subvideos * 30) > 3:
                output_file = f'{filename_root_output}{video_name}_video_{num_subvideos + 1}.mov'
                tasks.append(executor.submit(split_video_ffmpeg, filename, num_subvideos * 30, duration_video, output_file))
                num_subvideos += 1

            # Wait for all tasks to complete
            for task in tasks:
                task.result()

    print(f"Total sub-videos created: {num_subvideos}")
    return num_subvideos  # Ensure num_subvideos is returned if needed outside this function

# Get the duration of the video using ffprobe
def get_video_duration(filename):
    """Retrieve the duration of the video using ffprobe."""
    result = subprocess.run(
        ['ffprobe', '-v', 'error', '-show_entries', 'format=duration', '-of', 'default=noprint_wrappers=1:nokey=1', filename],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT
    )
    return float(result.stdout)

# Main execution starts here
start_time_ffmpeg = time.time()
print("Starting splitting video")

filename_root = f"/path-to-output-folder-for-files/{video_name}/"
filename_root_output = f"/path-to-output-folder-for-files/Output_Docs/{video_name}/"
filename = f'{filename_root}{video_name}.mov'

# Get video duration
duration_video = get_video_duration(filename)
print(f"duration_video: {duration_video}")

# Split the video
num_subvideos = split_video(video_name, duration_video, filename_root_output, filename)

end_time_ffmpeg = time.time()
duration_ffmpeg = end_time_ffmpeg - start_time_ffmpeg
print(f"Video splitting completed in {duration_ffmpeg:.2f} seconds.")
print("-------------------------------------------------------")
print("--- Processing finished for splitting the video. ---")
print("-------------------------------------------------------")
print("--- Running time: "+ str(duration_ffmpeg)  + "seconds ---")
print("-------------------------------------------------------")


#***********SPEECH FEATURE EXTRACTION*********
print("SPEECH FEATURE EXTRACTION was started...")
start_time_audio = time.time()

#************   myprosody         ***************
def run_praat_file(m, p):
    """
    p : path to dataset folder
    m : path to file
    returns : objects outputed by the praat script
    """
    sound=p+m+".wav"
    sourcerun='/path-to-myprosody/dataset/essen/myspsolution.praat'
    path=p

    #assert os.path.isfile(sound), "Wrong path to audio file"
    #assert os.path.isfile(sourcerun), "Wrong path to praat script"
    #assert os.path.isdir(path), "Wrong path to audio files"

 
    objects= run_file(sourcerun, -20, 2, 0.3, "yes",sound,path, 80, 400, 0.01, capture_output=True)
    #print (objects[0]) # This will print the info from the sound object, and objects[0] is a parselmouth.Sound object
    z1=str( objects[1]) # This will print the info from the textgrid object, and objects[1] is a parselmouth.Data object with a TextGrid inside
    z2=z1.strip().split()
    return z2

def mysptotal(m,p):
    """
    Overview
    """
    z2 = run_praat_file(m, p)
    z3=np.array(z2)
    z4=np.array(z3)[np.newaxis]
    z5=z4.T
    #dataset=pd.DataFrame({"number_ of_syllables":z5[0,:],"number_of_pauses":z5[1,:],"rate_of_speech":z5[2,:],"articulation_rate":z5[3,:],"speaking_duration":z5[4,:],
    #                    "original_duration":z5[5,:],"balance":z5[6,:],"f0_mean":z5[7,:],"f0_std":z5[8,:]})
    #print (dataset.T)
    speechrate_dictionary = {"number_ of_syllables":z5[0,:],"number_of_pauses":z5[1,:],"rate_of_speech":z5[2,:],"articulation_rate":z5[3,:],"speaking_duration":z5[4,:],
                        "original_duration":z5[5,:],"balance":z5[6,:],"f0_mean":z5[7,:],"f0_std":z5[8,:]}
    
    return speechrate_dictionary

# This function measures formants using Formant Position formula
def measureFormants(sound, f0min,f0max):
    sound = parselmouth.Sound(sound) # read the sound
    pitch = call(sound, "To Pitch (cc)", 0, f0min, 15, 'no', 0.03, 0.45, 0.01, 0.35, 0.14, f0max)
    pointProcess = call(sound, "To PointProcess (periodic, cc)", f0min, f0max)
    
    formants = call(sound, "To Formant (burg)", 0.0025, 5, 5000, 0.025, 50)
    numPoints = call(pointProcess, "Get number of points")

    f1_list = []
    f2_list = []
    f3_list = []
    f4_list = []
    
    # Measure formants only at glottal pulses
    for point in range(0, numPoints):
        point += 1
        t = call(pointProcess, "Get time from index", point)
        f1 = call(formants, "Get value at time", 1, t, 'Hertz', 'Linear')
        f2 = call(formants, "Get value at time", 2, t, 'Hertz', 'Linear')
        f3 = call(formants, "Get value at time", 3, t, 'Hertz', 'Linear')
        f4 = call(formants, "Get value at time", 4, t, 'Hertz', 'Linear')
        f1_list.append(f1)
        f2_list.append(f2)
        f3_list.append(f3)
        f4_list.append(f4)
    
    f1_list = [f1 for f1 in f1_list if str(f1) != 'nan']
    f2_list = [f2 for f2 in f2_list if str(f2) != 'nan']
    f3_list = [f3 for f3 in f3_list if str(f3) != 'nan']
    f4_list = [f4 for f4 in f4_list if str(f4) != 'nan']
    
    # calculate mean of formants across pulses
    f1_mean = mean(f1_list)
    f2_mean = mean(f2_list)
    f3_mean = mean(f3_list)
    f4_mean = mean(f4_list)
    
    # calculate stdev of formants across pulses
    f1_stdev = stdev(f1_list)
    f2_stdev = stdev(f2_list)
    f3_stdev = stdev(f3_list)
    f4_stdev = stdev(f4_list)
    
       
    return f1_mean, f2_mean, f3_mean, f4_mean, f1_stdev, f2_stdev, f3_stdev, f4_stdev, max(f2_list), min(f2_list)
#********************************************************

def new_speech_calculations(filename):
    silencedb = -25
    mindip = 2
    minpause = 0.3

    sound = parselmouth.Sound(filename)
    originaldur = sound.get_total_duration()
    intensity = sound.to_intensity(50)
    start = call(intensity, "Get time from frame number", 1)
    nframes = call(intensity, "Get number of frames")
    end = call(intensity, "Get time from frame number", nframes)
    min_intensity = call(intensity, "Get minimum", 0, 0, "Parabolic")
    max_intensity = call(intensity, "Get maximum", 0, 0, "Parabolic")

    # get .99 quantile to get maximum (without influence of non-speech sound bursts)
    max_99_intensity = call(intensity, "Get quantile", 0, 0, 0.99)

    # estimate Intensity threshold
    threshold = max_99_intensity + silencedb
    threshold2 = max_intensity - max_99_intensity
    threshold3 = silencedb - threshold2
    if threshold < min_intensity:
        threshold = min_intensity

    # get pauses (silences) and speakingtime
    textgrid = call(intensity, "To TextGrid (silences)", threshold3, minpause, 0.1, "silent", "sounding")
    silencetier = call(textgrid, "Extract tier", 1)
    silencetable = call(silencetier, "Down to TableOfReal", "sounding")
    npauses = call(silencetable, "Get number of rows")
    speakingtot = 0
    for ipause in range(npauses):
        pause = ipause + 1
        beginsound = call(silencetable, "Get value", pause, 1)
        endsound = call(silencetable, "Get value", pause, 2)
        speakingdur = endsound - beginsound
        speakingtot += speakingdur

    intensity_matrix = call(intensity, "Down to Matrix")
    # sndintid = sound_from_intensity_matrix
    sound_from_intensity_matrix = call(intensity_matrix, "To Sound (slice)", 1)
    # use total duration, not end time, to find out duration of intdur (intensity_duration)
    # in order to allow nonzero starting times.
    intensity_duration = call(sound_from_intensity_matrix, "Get total duration")
    intensity_max = call(sound_from_intensity_matrix, "Get maximum", 0, 0, "Parabolic")
    point_process = call(sound_from_intensity_matrix, "To PointProcess (extrema)", "Left", "yes", "no", "Sinc70")
    # estimate peak positions (all peaks)
    numpeaks = call(point_process, "Get number of points")
    t = [call(point_process, "Get time from index", i + 1) for i in range(numpeaks)]

    # fill array with intensity values
    timepeaks = []
    peakcount = 0
    intensities = []
    for i in range(numpeaks):
        value = call(sound_from_intensity_matrix, "Get value at time", t[i], "Cubic")
        if value > threshold:
            peakcount += 1
            intensities.append(value)
            timepeaks.append(t[i])

    # fill array with valid peaks: only intensity values if preceding
    # dip in intensity is greater than mindip
    validpeakcount = 0
    currenttime = timepeaks[0]
    currentint = intensities[0]
    validtime = []

    for p in range(peakcount - 1):
        following = p + 1
        followingtime = timepeaks[p + 1]
        dip = call(intensity, "Get minimum", currenttime, timepeaks[p + 1], "None")
        diffint = abs(currentint - dip)
        if diffint > mindip:
            validpeakcount += 1
            validtime.append(timepeaks[p])
        currenttime = timepeaks[following]
        currentint = call(intensity, "Get value at time", timepeaks[following], "Cubic")

    # Look for only voiced parts
    pitch = sound.to_pitch_ac(0.02, 30, 4, False, 0.03, 0.25, 0.01, 0.35, 0.25, 450)
    voicedcount = 0
    voicedpeak = []

    for time in range(validpeakcount):
        querytime = validtime[time]
        whichinterval = call(textgrid, "Get interval at time", 1, querytime)
        whichlabel = call(textgrid, "Get label of interval", 1, whichinterval)
        value = pitch.get_value_at_time(querytime) 
        if not math.isnan(value):
            if whichlabel == "sounding":
                voicedcount += 1
                voicedpeak.append(validtime[time])

    # calculate time correction due to shift in time for Sound object versus
    # intensity object
    timecorrection = originaldur / intensity_duration

    # Insert voiced peaks in TextGrid
    call(textgrid, "Insert point tier", 1, "syllables")
    for i in range(len(voicedpeak)):
        position = (voicedpeak[i] * timecorrection)
        call(textgrid, "Insert point", 1, position, "")
    
    if voicedcount==0: # NO SPEECH SUBVIDEO
        # return results
        #speakingrate = voicedcount / originaldur
        #articulationrate = voicedcount / speakingtot
        npause = npauses - 1
        #asd = speakingtot / voicedcount
        speechrate_dictionary = {'soundname':filename,
                                 'nsyll':0,
                                 'npause': npause,
                                 'dur(s)':originaldur,
                                 'phonationtime(s)':intensity_duration,
                                 'speechrate(nsyll / dur)': 0,
                                 "articulation rate(nsyll / phonationtime)":0,
                                 "ASD(speakingtime / nsyll)":0}
        return speechrate_dictionary
    
    else:
        # return results
        speakingrate = voicedcount / originaldur
        articulationrate = voicedcount / speakingtot
        npause = npauses - 1
        asd = speakingtot / voicedcount
        speechrate_dictionary = {'soundname':filename,
                                 'nsyll':voicedcount,
                                 'npause': npause,
                                 'dur(s)':originaldur,
                                 'phonationtime(s)':intensity_duration,
                                 'speechrate(nsyll / dur)': speakingrate,
                                 "articulation rate(nsyll / phonationtime)":articulationrate,
                                 "ASD(speakingtime / nsyll)":asd}
        return speechrate_dictionary


def get_files():
    files = glob('test_voices/*.wav')
    files.extend(glob('test_voices/*.mp3'))
    files.extend(glob('test_voices/*.ogg'))
    files.extend(glob('test_voices/*.aiff'))
    files.extend(glob('test_voices/*.aifc'))
    files.extend(glob('test_voices/*.au'))
    files.extend(glob('test_voices/*.nist'))
    files.extend(glob('test_voices/*.flac'))
    return files


  




#   EXTRACTING AUDIO FILE FROM VIDEO - 0.4 seconds per video

before_audio = time.time()
import moviepy.editor as mp

#print('Audio files is extracting from video files.')

for i in range(num_subvideos):
    my_clip = mp.VideoFileClip(r"/path-to-output-folder-for-files/Output_Docs/{}/{}_video_{}.mov".format(video_name,video_name,(i+1)))
    my_clip.audio.write_audiofile(r"/path-to-output-folder-for-files/Output_Docs/{}/{}_audio_{}.wav".format(video_name,video_name,(i+1)))

after_audio = time.time()
audio_time = after_audio - before_audio




#filename_root_file='{}{}_video'.format(filename_root_output,video_name)

def feature_calculation(csv_file_path):
    
    speech_features={}
    
    speech_features['B3_1_engagement_syllables']= {}
    speech_features['B3_2_engagement_avg_syllable_duration']= {}
    speech_features['B3_3_engagement_f2_range']= {}
    speech_features['B4_speech_rate']= {}
    speech_features['B5_1_pauses']= {}
    speech_features['B5_2_speaking_duration']= {}
    speech_features['B5_3_pause_duration']= {}
    speech_features['B5_4_original_duration']= {}
    speech_features['B5_5_speaking_balance']= {}
    speech_features['B5_6_speaking_duration_percentage']= {}
    speech_features['B5_7_voice_quality_pausing_duration_percentage']= {}
    speech_features['B6_articulation_rate']= {}
    speech_features['B7_1_voice_quality_f_mean']= {}
    speech_features['B7_2_voice_quality_localJitter']= {}
    speech_features['B7_3_voice_quality_localabsoluteJitter']= {}
    speech_features['B7_4_voice_quality_localShimmer']= {}
    speech_features['B7_5_voice_quality_localdbShimmer']= {}
    speech_features['B7_6_voice_quality_mean_harmonics_to_noise']= {}
    speech_features['B7_7_voice_quality_f1_mean']= {}
    speech_features['B7_8_voice_quality_f2_mean']= {}
    speech_features['B7_9_voice_quality_f3_mean']= {}
    speech_features['B7_1_voice_quality_f4_mean']= {}
    speech_features['B7_11_voice_quality_amplitude_diff_H1_A3']= {}
    speech_features['B3_B8_1_linguistic_stress_pitch_avg']= {}
    speech_features['B3_B8_5_linguistic_stress_mean_intensity_all_mean']= {}
    
    
    syllables = []
    pauses = []
    speech_rate = []
    articulation_rate = []
    speaking_duration = []
    original_duration = []
    balance = []
    f0_mean = []
    f0_std = []
    
    max_f2_list_all = []
    min_f2_list_all = []
    pitchs = []
    mean_intensity_all = []
    
    total_ss = 0
    total_asd = 0
    total_ps = 0
    total_srs = 0
    total_ars = 0
    total_sds = 0
    total_ods = 0
    total_bs = 0
    total_fsm = 0
    total_fss = 0
    
    total_mean_pitch = 0
    total_localJitter = 0
    total_localabsoluteJitter = 0
    total_localShimmer = 0
    total_localdbShimmer = 0
    total_unvoiced_frames_percentage = 0
    total_degree_voice_breaks_percentage = 0
    total_number_voice_breaks = 0
    total_harmonics_to_noise = 0
    total_f1_mean = 0
    total_f2_mean = 0
    total_f3_mean = 0
    total_f4_mean = 0
   
    #print(i)
    
    #filename_sound='{}_{}_audio.wav'.format(filename_root_file,(i+1))        
    #title = '{}_video_{}_audio'.format(video_name,i+1)    
    filename_with_ext = os.path.basename(csv_file_path)
    #print('filename_with_ext ',filename_with_ext)
    title =os.path.splitext(filename_with_ext)[0]
    #print('title ',title)
    #file = filename_root_output
    file = os.path.dirname(csv_file_path)+'/'
    #print('file ',file)
    speech_features = mysptotal(title,file) # myprosody
    
    f0min = 75
    f0max = 500    
    #Main Praat scripts    
    new_speech_features=new_speech_calculations(csv_file_path)    
    new_articulation_rate=new_speech_features['articulation rate(nsyll / phonationtime)']
    new_syllables=new_speech_features['nsyll']
    new_original_duration=new_speech_features['dur(s)']
    new_phonation_time=new_speech_features['phonationtime(s)']
    new_speech_rate=new_speech_features['speechrate(nsyll / dur)']
    new_avg_syllable_duration=new_speech_features['ASD(speakingtime / nsyll)']
    
    sound = parselmouth.Sound(csv_file_path)
    pitch = sound.to_pitch()
    pulses = parselmouth.praat.call([sound, pitch], "To PointProcess (cc)")
    voice_report_str = parselmouth.praat.call([sound, pitch, pulses], "Voice report", 0.0, 0.0, 75, 600, 1.3, 1.6, 0.03, 0.45)
    #speech_features = mysptotal(title,file) # myprosody
    pointProcess = call(sound, "To PointProcess (periodic, cc)", f0min, f0max)    
    report = voice_report_str
    (f1_mean, f2_mean, f3_mean, f4_mean, f1_stdev, f2_stdev, f3_stdev, f4_stdev, max_f2_list, min_f2_list) = measureFormants(sound,75,500)
   
    #extracting auido features    
    
    #syllables_subvideo = (np.array(speech_features['number_ of_syllables']))
    #ss = (float(syllables_subvideo[0]))
    ss = new_syllables
    total_ss = total_ss + ss
    
    #average syllable duration  (speakingtime / nsyll)
    asd = new_avg_syllable_duration
    total_asd = total_asd + asd
    
    pauses_subvideo = np.array(speech_features['number_of_pauses'])
    #print(pauses_subvideo)
    ps = (float(pauses_subvideo[0]))
    total_ps = total_ps + ps
    
    #speech_rate_subvideo = np.array(speech_features['rate_of_speech'])
    #srs = (float(speech_rate_subvideo[0]))
    srs = new_speech_rate
    total_srs = total_srs + srs
        
    #articulation_rate_subvideo = np.array(speech_features['articulation_rate'])
    #ars = (float(articulation_rate_subvideo[0]))
    ars = new_articulation_rate
    total_ars = total_ars + ars
    
    speaking_duration_subvideo = np.array(speech_features['speaking_duration'])
    sds = (float(speaking_duration_subvideo[0])) 
    total_sds = total_sds + sds
    
    original_duration_subvideo = np.array(speech_features['original_duration'])
    ods = (float(original_duration_subvideo[0]))
    total_ods = total_ods + ods    
   
    bs = sds/ods
    total_bs = total_bs + bs
    
    f0_mean_subvideo = np.array(speech_features['f0_mean'])
    fsm = (float(f0_mean_subvideo[0]))
    total_fsm = total_fsm + fsm
    
    f0_std_subvideo = np.array(speech_features['f0_std'])
    fss = (float(f0_std_subvideo[0]))
    total_fss = total_fss + fss
    
    mean_pitch = parselmouth.praat.call(pitch, "Get mean", 0.0, 0.0, "Hertz")
    total_mean_pitch = total_mean_pitch + mean_pitch
    pitchs.append(mean_pitch)
    
    localJitter = call(pointProcess, "Get jitter (local)", 0, 0, 0.0001, 0.02, 1.3)
    total_localJitter = total_localJitter + localJitter
    
    localabsoluteJitter = call(pointProcess, "Get jitter (local, absolute)", 0, 0, 0.0001, 0.02, 1.3)
    total_localabsoluteJitter = total_localabsoluteJitter + localabsoluteJitter
    
    localShimmer =  call([sound, pointProcess], "Get shimmer (local)", 0, 0, 0.0001, 0.02, 1.3, 1.6)
    total_localShimmer = total_localShimmer + localShimmer
    
    localdbShimmer = call([sound, pointProcess], "Get shimmer (local_dB)", 0, 0, 0.0001, 0.02, 1.3, 1.6)
    total_localdbShimmer = total_localdbShimmer + localdbShimmer
        
    unvoiced_frames_index = report.find("Fraction of locally unvoiced frames: ")
    unvoiced_frames_percentage = int(float(voice_report_str[unvoiced_frames_index+37:unvoiced_frames_index+43]))
    total_unvoiced_frames_percentage = total_unvoiced_frames_percentage + unvoiced_frames_percentage
    
    degree_voice_breaks_index = report.find("Degree of voice breaks: ")
    degree_voice_breaks_percentage = int(float(voice_report_str[degree_voice_breaks_index+24:degree_voice_breaks_index+29]))
    total_degree_voice_breaks_percentage = total_degree_voice_breaks_percentage + degree_voice_breaks_percentage
    
    voice_breaks_index = report.find("Number of voice breaks: ")    
    number_voice_breaks = int(float(voice_report_str[voice_breaks_index+23:voice_breaks_index+26]))
    total_number_voice_breaks = total_number_voice_breaks + number_voice_breaks
    
    harmonics_to_noise_index = report.find("Mean harmonics-to-noise ratio: ")
    harmonics_to_noise = int(float(voice_report_str[harmonics_to_noise_index+31:harmonics_to_noise_index+36]))
    total_harmonics_to_noise = total_harmonics_to_noise + harmonics_to_noise
    
    total_f1_mean = total_f1_mean + f1_mean
    total_f2_mean = total_f2_mean + f2_mean
    total_f3_mean = total_f3_mean + f3_mean
    total_f4_mean = total_f4_mean + f4_mean
    
    max_f2_list_all.append(max_f2_list)
    min_f2_list_all.append(min_f2_list)
    
    mean_intensity = parselmouth.praat.call(parselmouth.Sound(sound).to_intensity(), "Get mean", 0, 0, "energy")
    mean_intensity_all.append(mean_intensity)      

    f2_range_max = max(max_f2_list_all)
    f2_range_min = min(min_f2_list_all)

    
    df1 = pd.DataFrame([[float("{0:.2f}".format(total_srs))],
                 [float("{0:.2f}".format(total_f1_mean))], 
    			 [float("{0:.2f}".format(total_f2_mean))],
    			 [float("{0:.2f}".format(total_f3_mean))], 
    			 [float("{0:.2f}".format(total_f4_mean))], 
                 [float("{0:.2f}".format(total_f3_mean-total_fsm))],             
    			 [float("{0:.2f}".format(total_mean_pitch))], 
                 [float("{0:.2f}".format(mean(mean_intensity_all)))],
                 [float("{0:.2f}".format(f2_range_max))],
                 [float("{0:.2f}".format(f2_range_min))],
                 [float("{0:.2f}".format(f2_range_min))],
                 [float("{0:.2f}".format(total_fsm))],
                 [float("{0:.3f}".format(total_localJitter))],               
                 [float("{0:.2f}".format(total_ss))], 
                 [float("{0:.2f}".format(total_ps))],
                 [float("{0:.2f}".format(total_bs))],
                 [float("{0:.2f}".format(total_ars))],
                 [float("{0:.6f}".format(total_localabsoluteJitter))], 
    			 [float("{0:.2f}".format(total_localShimmer))], 
                 [float("{0:.2f}".format(total_localdbShimmer))], 
                 [float("{0:.2f}".format(total_harmonics_to_noise))],                    
                 [float("{0:.2f}".format(total_asd))],
                 [float("{0:.2f}".format(total_sds))],
    			 [float("{0:.2f}".format(total_ods-total_sds))],                
                 [float("{0:.2f}".format(100*total_bs))], 
                 [float("{0:.2f}".format(100-(100*total_bs)))]],columns=['Speech'])

# Save results to a separate CSV file   
    output_file = f"/path-to-output-folder-for-files/Output_Docs/{video_name}/output_{os.path.splitext(csv_file_path.split('/')[-1])[0]}.csv"
    
    df1.to_csv(output_file, index=False)

processes=os.cpu_count()
#processes= 4

def process_files_in_parallel(file_paths):
    with multiprocessing.Pool(processes) as pool:
        pool.map(feature_calculation, file_paths)

#file_paths = ["file1.csv", "file2.csv", "file3.csv"]  # Replace with your list of files
file_paths = [    f"/path-to-output-folder-for-files/Output_Docs/{video_name}/{video_name}_audio_{i+1}.wav"
    for i in range(num_subvideos)
]

process_files_in_parallel(file_paths)

def calculate_row_stats(file_pattern):
  # Read all CSV files into a list of DataFrames
  dfs = [pd.read_csv(file, names=['Speech'], skiprows=1,) for file in glob.glob(file_pattern)]

  # Concatenate the DataFrames along the columns
  combined_df = pd.concat(dfs, axis=1)

  # Convert values to numeric
  combined_df = combined_df.astype(float)

  # Calculate statistics
  result = {}
  for i in range(26):
      if i == 8:
          result[f'Speech_{i+1}'] = combined_df.iloc[i].max()
      if i == 9:
          result[f'Speech_{i+1}'] = combined_df.iloc[i].min()
      if i == 10:
          result[f'Speech_{i+1}'] =result[f'Speech_{i-1}'] - result[f'Speech_{i}'] 
      else:
          result[f'Speech_{i+1}'] = combined_df.iloc[i].mean() 
  

  return result

file_pattern = '/path-to-output-folder-for-files/Output_Docs/'+video_name+'/output_'+video_name+'_audio_*.csv'  # Replace with your actual file pattern
#print("file_pattern: "+str(file_pattern))
result = calculate_row_stats(file_pattern)

#print(result)

names=['B1_speech_rate',
                  'B2_engagement_f1_mean',
 				  'B2_engagement_f2_mean',
                  'B2_engagement_f3_mean',
				  'B2_engagement_f4_mean',
				  'B2_engagement_amplitude_diff_H1_A3',                  
 				  'B2_engagement_pitch_avg',                  
				  'B2_engagement_intensity_mean',
                  'B2_engagement_f2_range_max',
                  'B2_engagement_f2_range_min',
                  'B2_engagement_f2_range',
                  'B2_engagement_f0_mean',
 				  'B2_engagement_localJitter',                    
                  'B3_voiced_syllables',
                  'B3_number_of_pauses',				 
                  'B3_speaking_balance',
                  'B4_articulation_rate',
                  'B5_voice_quality_localabsoluteJitter',
				  'B5_voice_quality_localShimmer',
				  'B5_voice_quality_localdbShimmer',
				  'B5_voice_quality_mean_harmonics_to_noise',                   
                  'B6_monotonous_speech_syllable_duration',
                  'B6_monotonous_speech_speaking_duration',
				  'B6_monotonous_speech_pausing_duration',  
				  'B6_monotonous_speech_speaking_duration_percentage',
                  'B6_monotonous_speech_pausing_duration_percentage']

result_last = pd.DataFrame({'Speech': [result[f'Speech_{i+1}'] for i in range(26)]}, index=names)
 
#print(result_last)

output_file = "/path-to-output-folder-for-files/Output_Features/{}/{}_speech.json".format(video_name,video_name)

result_last.to_json(output_file)


speech_time = time.time() - start_time_audio

print("---Audio Files Extraction is ended in %s seconds ---" % (audio_time))
print("---Speech Feature Extraction is ended in %s seconds ---" % (speech_time))


