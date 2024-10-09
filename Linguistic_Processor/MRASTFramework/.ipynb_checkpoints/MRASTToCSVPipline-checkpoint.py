'''
Created on 22. feb. 2024

@author: izido
'''

from lib.LanguageProcessor import LanguageProcessor

import csv
import os
import sys
import re 
import json 
import statistics
from pathlib import Path
import matplotlib.pyplot as plt
import pandas as pd


LANGUAGE_ANALYSIS_OUTPUT_FILE = 'data.language.SM.details.json';
LANGUAGE_ANALYSIS_SUMMARYCS_FILE = 'data.language.SM.CS.json';
LANGUAGE_ANALYSIS_SUMMARY_CSV_FILE = 'data.language.SM.summary-new.csv';
LANGUAGE_ANALYSIS_SUMMARY_PATH ="summary/";
LANGUAGE_ANALYSIS_OVERALL_PATH ="summary/";


def CreateCSV(inputjson = LANGUAGE_ANALYSIS_OUTPUT_FILE, outputjson = LANGUAGE_ANALYSIS_SUMMARYCS_FILE, outputcsv = LANGUAGE_ANALYSIS_SUMMARY_CSV_FILE, summarypath = LANGUAGE_ANALYSIS_SUMMARY_PATH):
    languageprocessor = LanguageProcessor()
    analysis = languageprocessor.PrepareforCSV(summarypath+inputjson);
    print("**********Wirtting overall to file:*************", summarypath + outputjson)
    with open(summarypath + outputjson, 'w', encoding='utf-8') as f:
       json.dump(analysis, f, ensure_ascii=False, indent=4)#
    
    totalvideos = analysis["total_videos"];
    csvheader=list(analysis.keys())
    i = 0;
    
    print(csvheader)
    csvheader.remove("A2_sentiment_words")
    csvheader.remove("A2_sentiment_sentece")
    csvheader.remove("total_videos")
    print(analysis)
    
    csvdataarray = []
    for i in range(totalvideos):
        item = []
        print("*************",i,"**********************")
        for key in csvheader:
            print("*+++*",key,"*++*")
            item.append(analysis[key][i]);
        csvdataarray.append(item);
   
    with open(summarypath+outputcsv, 'w', encoding='UTF8', newline='') as f:
         writer = csv.writer(f,delimiter=";")
         writer.writerow(csvheader)
         writer.writerows(csvdataarray)
        
    print("**********Wirtting overall to CSV:*************", summarypath + outputcsv)
    
    
    
print("+++++++++++++++++++++++++++++++++Create CSV++++++++++++++++++++++++++++++++++++++++++")
CreateCSV();