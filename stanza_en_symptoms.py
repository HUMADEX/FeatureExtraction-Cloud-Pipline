import stanza
from glob import glob
import pandas as pd 
import json
import os
import sys

import time

start_time = time.time()

video_name = sys.argv[1]
video_name = video_name.split(".")[0]  # Split by "." and take the first part

asr_text_result = sys.argv[2]


stanza.download('en', package='mimic', processors={'ner':'i2b2'})
nlp = stanza.Pipeline('en', package='mimic', processors={'ner':'i2b2'})

#file = glob('/path-to-output-folder-for-files/Linguistic_Processor/MRASTFramework/input/transcriptions-SM-1/*.csv')[0]

#Stanza_doc_open = open(file, 'r').read()
#doc_file = nlp(Stanza_doc_open)

doc_file = nlp(asr_text_result)

Symptoms = []
for ent in doc_file.entities:
    if ent.type == "PROBLEM":
        Symptoms.append(ent.text)
        
print(Symptoms)

df = pd.DataFrame(Symptoms)
#df.to_csv("/path-to-output-folder-for-files/Output_Features/Symptoms.csv")

#df.to_json(r"/path-to-output-folder-for-files/Output_Features/Symptoms.json")
df.to_json(r"/path-to-output-folder-for-files/Output_Features/{}/{}_symptoms.json".format(video_name,video_name))

#print("---Symptom extraction is ended in %s seconds ---" % (time.time() - start_time))