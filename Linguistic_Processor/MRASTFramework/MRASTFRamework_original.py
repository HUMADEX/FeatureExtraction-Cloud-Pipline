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

"""
########DAICWOZ CONFIG############################################
LANGUAGE_ANALYSIS_OUTPUT_FILE = 'data.language.DAIC.details.json';

LANGUAGE_ANALYSIS_OUTPUT_PATH ="summary/";
LANGUAGE_ANALYSIS_SUMMARY_FILE = 'data.language.DAIC.summary.json';

LANGUAGE_ANALYSIS_SUMMARYCS_FILE = 'data.language.DAIC.CS.json';
LANGUAGE_ANALYSIS_SUMMARY_CSV_FILE = 'data.language.DAIC.summary.csv';

LANGUAGE_ANALYSIS_SUMMARY_PATH ="summary/";
LANGUAGE_ANALYSIS_OVERALL_FILE = 'data.language.DAIC.overall.json';
LANGUAGE_ANALYSIS_OVERALL_PATH ="summary/";
INDIVIDUAL_ANALYSIS_FILE = "analysis.language.DAIC.%s.json"
INDIVIDUAL_ANALYSIS_PATH = "output/daic/"
PATH_TRANSCRIPTIONS = "input/transcriptions-DAIC/"
"""
########SYMPTOMMEDIA CONFIG############################################
LANGUAGE_ANALYSIS_OUTPUT_FILE = 'data.language.SM.details.json';
LANGUAGE_ANALYSIS_OUTPUT_PATH ="/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Linguistic_Processor/MRASTFramework/summary-new/";
LANGUAGE_ANALYSIS_SUMMARY_FILE = 'data.language.SM.summary.json';

LANGUAGE_ANALYSIS_SUMMARYCS_FILE = 'data.language.SM.CS.json';
LANGUAGE_ANALYSIS_SUMMARY_CSV_FILE = 'data.language.SM.summary.csv';

LANGUAGE_ANALYSIS_SUMMARY_PATH ="/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Linguistic_Processor/MRASTFramework/summary-new/";
LANGUAGE_ANALYSIS_OVERALL_FILE = 'data.language.SM.overall.json';
LANGUAGE_ANALYSIS_OVERALL_PATH ="/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Linguistic_Processor/MRASTFramework/summary-new/";
INDIVIDUAL_ANALYSIS_FILE = "analysis.language.SM.%s.json"
INDIVIDUAL_ANALYSIS_PATH = "/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Linguistic_Processor/MRASTFramework/output/symmedia-new/"
PATH_TRANSCRIPTIONS = "/Dockers/Datasets/libraries/E2E_Pipeline/SMILE_project/SMILE_Observable_Cues/Linguistic_Processor/MRASTFramework/input/transcriptions-SM-1/"
#DO NOT FORGET TO UPDATE MAP_DEPRESSED in LanguageProcessor


def ProcessLanguageRawfromTranscriptions(pathanalysisdetailes = INDIVIDUAL_ANALYSIS_PATH, analysisfiles=INDIVIDUAL_ANALYSIS_FILE,  source_transcriptions = PATH_TRANSCRIPTIONS, detailanalysis_path= LANGUAGE_ANALYSIS_OUTPUT_PATH , detailanalysis_file = LANGUAGE_ANALYSIS_OUTPUT_FILE):
        input = load_data(source_transcriptions);
        languageprocessor = LanguageProcessor()
        
        language_analysis = {}; 
        
        print(input);
        
        for id in input:
            print("---*************Start with transcription: " + id + "*****************************----") 
             
            with open(input[id]["transcription"], encoding='utf-8-sig') as file:
                lines = file.readlines()
                lines = [line.strip('"').strip() for line in lines]
                
                if len(lines) > 0:            
                    LA = languageprocessor.ProcessTranscription(lines, id)
                    language_analysis[id] = LA
                    filename = pathanalysisdetailes + (analysisfiles % (str(id)))
                    with open(filename, 'w', encoding='utf-8') as f:
                        json.dump(language_analysis[id], f, ensure_ascii=False, indent=4)
                    print("---*************Analysis stored: " + filename + "*****************************----")  
                   
            print("---*************End transcription: " + id + "*****************************----") 
                 #print("---*************FINALE: " + id + "*****************************----") 
        
        print("******************************************")   
    
     
        with open(detailanalysis_path + detailanalysis_file, 'w', encoding='utf-8') as f:
            json.dump(language_analysis, f, ensure_ascii=False, indent=4)#
            
        print("Language Analysis done.... Data written to:", detailanalysis_path + detailanalysis_file) 
        print("******************************************")   

def PostProcessLanguageCreateSummary(summaryfile = LANGUAGE_ANALYSIS_SUMMARY_FILE, summarypath = LANGUAGE_ANALYSIS_SUMMARY_PATH, pathanalysisdetailes = LANGUAGE_ANALYSIS_OUTPUT_PATH, analysisfile=LANGUAGE_ANALYSIS_OUTPUT_FILE, overallfile = LANGUAGE_ANALYSIS_OVERALL_FILE, overallpath =  LANGUAGE_ANALYSIS_OVERALL_PATH):
    print("Finaly Analysis Starts") 
    print("******************************************")   
    languageprocessor = LanguageProcessor()
  
    output, depressed, nondepressed = languageprocessor.PostProcessCreateSummary(pathanalysisdetailes+analysisfile) 
    
    with open(summarypath + summaryfile, 'w', encoding='utf-8') as f:
        json.dump(output, f, ensure_ascii=False, indent=4)#
            
    
       
    print("Language Analysis done....Summary written to:", summarypath+summaryfile, "hasdepressed=", depressed, "hasnondepressed=",nondepressed) 
    print("******************************************")    
    
    VisualizeSummary(output, printout=False, hasdepressed=depressed, hasnondepressed=nondepressed, summaryfile=overallfile, summarypath=overallpath)
    
    print("Final overall anaylsis done....Summary written to:", overallpath+overallfile) 
    print("******************************************")    

    


def load_data(sourcetranscript):
     inpudata = {}

     for path,dirs,files in os.walk(sourcetranscript):
        for filename in files:
           
            inputtoken = {}
            inputtoken["transcription"] = os.path.join(path,filename)
            #video_id = filename.split("_")[0] 
            video_id = filename
            inpudata[video_id] = inputtoken;
     return inpudata;

 

def VisualizeSummary(data, printout = True, hasdepressed = True, hasnondepressed = True, summaryfile = LANGUAGE_ANALYSIS_OVERALL_FILE, summarypath = LANGUAGE_ANALYSIS_OVERALL_PATH):
    ngw_depressive = sum(data["depressive"]['wordsentimentraw']['negative']);
    ngw_ndepressive = sum(data["nondepressive"]['wordsentimentraw']['negative']);
    
    nuw_depressive = sum(data["depressive"]['wordsentimentraw']['neutral']);
    nuw_ndepressive = sum(data["nondepressive"]['wordsentimentraw']['neutral']);
    
    pw_depressive = sum(data["depressive"]['wordsentimentraw']['positive']);
    pw_ndepressive = sum(data["nondepressive"]['wordsentimentraw']['positive']);
    
    statistcs = {"base": {}, "pastwords":{}, "pronoun_fps":{}, "wordsentiment":{}, "sentecesentiment":{}, "absoultis_fps":{},
                 "depressionwords":{}, "sentecelength_wordspersentece":{}, "senteceuniquew_TTR":{}, "complexity_clauses_perstatement":{},
                 "lexdensity":{},"lexialspohistication":{}, 
                 "lexialdensity_correct_specificPOS": {},"lexialdensity_correct": {"persentece":{}, "pervideo": {}, "persentece_nofeats":{}, "pervideo_nofeats": {}, "pervideo_normalized":{}, "pervideo_nofeats_normalized": {}}
                 }
    if hasdepressed:
        statistcs["base"]["D total_words"] = data["depressive"]["total_words"]
        statistcs["base"]["D total_uniuqe"] = data["depressive"]["total_uniuqe"]
        statistcs["base"]["D total_senteces"] = data["depressive"]["total_senteces"]
        statistcs["base"]["D total_statements"]= data["depressive"]["total_statements"]
        statistcs["base"]["D total_clauses"] = data["depressive"]["total_clauses"]
        
        
        
        statistcs["pastwords"]["D"] = data["depressive"]["pastwords"]
        statistcs["pastwords"]["D per 1000"] = round((data["depressive"]["total_past_words"]/data["depressive"]["total_words"])*1000,2)
        statistcs["pronoun_fps"]["D"] = data["depressive"]["pronoun_fps"]
        statistcs["pronoun_fps"]["D per 1000"] = round((data["depressive"]["total_first_person_p"]/data["depressive"]["total_words"])*1000,2)
        statistcs["wordsentiment"]["D"] = data["depressive"]["overallws"] 
        statistcs["wordsentiment"]["D negativewords per 1000"] = round((ngw_depressive/data["depressive"]["total_words"])*1000,2)
        statistcs["wordsentiment"]["D positivewords per 1000"] = round((pw_depressive/data["depressive"]["total_words"])*1000,2)
        statistcs["wordsentiment"]["D neutralwords per 1000"] = round((nuw_depressive/data["depressive"]["total_words"])*1000,2)
        statistcs["sentecesentiment"]["D"] = data["depressive"]["overallsens"]
        statistcs["absoultis_fps"]["D"] = data["depressive"]["absoultistwords"]
        statistcs["absoultis_fps"]["D per 1000"] = round((sum(data["depressive"]["absoultistwords"]["rawdata"])/data["depressive"]["total_words"])*1000,2)
        statistcs["depressionwords"]["D"] = data["depressive"]["depressionwords"]
        statistcs["depressionwords"]["D per 1000"] = round((sum(data["depressive"]["depressionwords"]["rawdata"])/data["depressive"]["total_words"])*1000,2) 
        statistcs["sentecelength_wordspersentece"]["D"] = data["depressive"]["sentecelength"]
        statistcs["senteceuniquew_TTR"]["D"] = data["depressive"]["senteceuniquew"]
        statistcs["complexity_clauses_perstatement"]["D"] = data["depressive"]["clausesperstatement"]
        statistcs["lexdensity"]["D"] = data["depressive"]["lexdensity"]
        
        
        
        statistcs["lexialspohistication"]["D"] = data["depressive"]["lexialspohistication"]
        
        statistcs["lexialdensity_correct_specificPOS"]["D"] = data["depressive"]["lexdensity_tokens"]
        
        statistcs["lexialdensity_correct"]["persentece"]["D"] = data["depressive"]["lexdensity_senetce"]
        statistcs["lexialdensity_correct"]["pervideo"]["D"] = data["depressive"]["lexdensity_video"]
        statistcs["lexialdensity_correct"]["persentece_nofeats"]["D"] = data["depressive"]["lexdensity_senetce_nofeats"]
        statistcs["lexialdensity_correct"]["pervideo_nofeats"]["D"] = data["depressive"]["lexdensity_video_nofeats"]
        
        statistcs["lexialdensity_correct"]["pervideo_normalized"]["D"] = data["depressive"]["avg_lexdensity_video_nofeats_normalized"]
        statistcs["lexialdensity_correct"]["pervideo_nofeats_normalized"]["D"] = data["depressive"]["avg_lexdensity_video_nofeats_normalized"]

    if hasnondepressed:      
        statistcs["base"]["ND total_words"] = data["nondepressive"]["total_words"]
        statistcs["base"]["ND total_uniuqe"] = data["nondepressive"]["total_uniuqe"]
        statistcs["base"]["ND total_senteces"] = data["nondepressive"]["total_senteces"]
        statistcs["base"]["ND total_statements"] = data["nondepressive"]["total_statements"]
        statistcs["base"]["ND total_clauses"] = data["nondepressive"]["total_clauses"] 
        statistcs["pastwords"]["ND"] = data["nondepressive"]["pastwords"]
        statistcs["pastwords"]["ND per 1000"] = round((data["nondepressive"]["total_past_words"]/data["nondepressive"]["total_words"])*1000,2)
        statistcs["pronoun_fps"]["ND"] = data["nondepressive"]["pronoun_fps"]
        statistcs["pronoun_fps"]["ND per 1000"] = round((data["nondepressive"]["total_first_person_p"]/data["nondepressive"]["total_words"])*1000,2)
        statistcs["wordsentiment"]["ND"] = data["nondepressive"]["overallws"]
        statistcs["wordsentiment"]["ngw_depressive"] = ngw_depressive, data["depressive"]["total_negativ_words"]
        statistcs["wordsentiment"]["ngw_ndepressive"] = ngw_ndepressive, data["nondepressive"]["total_negativ_words"]
        statistcs["wordsentiment"]["ND negativewords per 1000"] = round((ngw_ndepressive/data["nondepressive"]["total_words"])*1000,2)   
        statistcs["wordsentiment"]["ND positivewords per 1000"] = round((pw_ndepressive/data["nondepressive"]["total_words"])*1000,2)    
        statistcs["wordsentiment"]["ND neutralwords per 1000"] = round((nuw_ndepressive/data["nondepressive"]["total_words"])*1000,2)                          
        statistcs["sentecesentiment"]["ND"] = data["nondepressive"]["overallsens"]
        statistcs["absoultis_fps"]["ND"] = data["nondepressive"]["absoultistwords"]
        statistcs["absoultis_fps"]["ND per 1000:"] = round((sum(data["nondepressive"]["absoultistwords"]["rawdata"])/data["nondepressive"]["total_words"])*1000,2)
        statistcs["depressionwords"]["ND"] = data["nondepressive"]["depressionwords"]
        statistcs["depressionwords"]["ND per 1000"] = round((sum(data["nondepressive"]["depressionwords"]["rawdata"])/data["nondepressive"]["total_words"])*1000,2)
        statistcs["sentecelength_wordspersentece"]["ND"] = data["nondepressive"]["sentecelength"]
        statistcs["senteceuniquew_TTR"]["ND"] = data["nondepressive"]["senteceuniquew"]
        statistcs["complexity_clauses_perstatement"]["ND"] = data["nondepressive"]["clausesperstatement"]
        statistcs["lexdensity"]["ND"] = data["nondepressive"]["lexdensity"]
        statistcs["lexialspohistication"]["ND"] = data["nondepressive"]["lexialspohistication"]
        
        statistcs["lexialdensity_correct_specificPOS"]["ND"] = data["nondepressive"]["lexdensity_tokens"]
        
        statistcs["lexialdensity_correct"]["persentece"]["ND"] = data["nondepressive"]["lexdensity_senetce"]
        statistcs["lexialdensity_correct"]["pervideo"]["ND"] = data["nondepressive"]["lexdensity_video"]
        statistcs["lexialdensity_correct"]["persentece_nofeats"]["ND"] = data["nondepressive"]["lexdensity_senetce_nofeats"]
        statistcs["lexialdensity_correct"]["pervideo_nofeats"]["ND"] = data["nondepressive"]["lexdensity_video_nofeats"]
        statistcs["lexialdensity_correct"]["pervideo_normalized"]["D"] = data["depressive"]["avg_lexdensity_video_nofeats_normalized"]
        statistcs["lexialdensity_correct"]["pervideo_nofeats_normalized"]["D"] = data["depressive"]["avg_lexdensity_video_nofeats_normalized"]
        

    print("**********Wirtting overall to file:*************", summarypath + summaryfile)
    with open(summarypath + summaryfile, 'w', encoding='utf-8') as f:
        json.dump(statistcs, f, ensure_ascii=False, indent=4)#
            
    if printout: 
        print("------------basedata---------------") 
        print("D total_words", data["depressive"]["total_words"])
        print("D total_uniuqe", data["depressive"]["total_uniuqe"])
        print("D total_senteces", data["depressive"]["total_senteces"])
        print("D total_statements", data["depressive"]["total_statements"])
        print("D total_clauses", data["depressive"]["total_clauses"])
        
        print("ND total_words", data["nondepressive"]["total_words"])
        print("ND total_uniuqe", data["nondepressive"]["total_uniuqe"])
        print("ND total_senteces", data["nondepressive"]["total_senteces"])
        print("ND total_statements", data["nondepressive"]["total_statements"])
        print("ND total_clauses", data["nondepressive"]["total_clauses"])  
              
        
        print("------------pastwords---------------")
        print("D", data["depressive"]["pastwords"])
        print("normalized per 1000:", round((data["depressive"]["total_past_words"]/data["depressive"]["total_words"])*1000,2))
        
        print("ND", data["nondepressive"]["pastwords"]) 
        print("normalized per 1000:", round((data["nondepressive"]["total_past_words"]/data["nondepressive"]["total_words"])*1000,2))
        
        print("--------------pronoun_fps-------------")
        print("D", data["depressive"]["pronoun_fps"])
        print("normalized per 1000:", round((data["depressive"]["total_first_person_p"]/data["depressive"]["total_words"])*1000,2))
        print("ND", data["nondepressive"]["pronoun_fps"])
        print("normalized per 1000:", round((data["nondepressive"]["total_first_person_p"]/data["nondepressive"]["total_words"])*1000,2))
        
        print("--------------word sentiment-------------")
        print("D", data["depressive"]["overallws"] )
        
        print("ND", data["nondepressive"]["overallws"])
        print("ngw_depressive", ngw_depressive, data["depressive"]["total_negativ_words"])
        print("ngw_ndepressive", ngw_ndepressive, data["nondepressive"]["total_negativ_words"])
        print("normalized negative despressive per 1000:", round((ngw_depressive/data["depressive"]["total_words"])*1000,2))
        print("normalized negative non-despressive per 1000:", round((ngw_ndepressive/data["nondepressive"]["total_words"])*1000,2))
        
        print("normalized postitve despressive per 1000:", round((pw_depressive/data["depressive"]["total_words"])*1000,2))
        print("normalized positive non-despressive per 1000:", round((pw_ndepressive/data["nondepressive"]["total_words"])*1000,2))
        
        
        print("--------------sentece sentiment-------------")
        print("D", data["depressive"]["overallsens"] )
        
        print("ND", data["nondepressive"]["overallsens"])
        
        
        print("--------------absoultis_fps-------------")
        print("D", data["depressive"]["absoultistwords"])
        print("normalized per 1000:", round((sum(data["depressive"]["absoultistwords"]["rawdata"])/data["depressive"]["total_words"])*1000,2))
        print("ND", data["nondepressive"]["absoultistwords"])
        print("normalized per 1000:", round((sum(data["nondepressive"]["absoultistwords"]["rawdata"])/data["nondepressive"]["total_words"])*1000,2))
        
        
        print("--------------depressionwords-------------")
        print("D", data["depressive"]["depressionwords"])
        print("normalized per 1000:", round((sum(data["depressive"]["depressionwords"]["rawdata"])/data["depressive"]["total_words"])*1000,2))
        print("ND", data["nondepressive"]["depressionwords"])
        print("normalized per 1000:", round((sum(data["nondepressive"]["depressionwords"]["rawdata"])/data["nondepressive"]["total_words"])*1000,2))
        
        
        
        print("------------sentecelength - words per sentece ---------------")
        print("D", data["depressive"]["sentecelength"])
        print("ND", data["nondepressive"]["sentecelength"])
        
        print("------------senteceuniquew - TTR ---------------")
        print("D", data["depressive"]["senteceuniquew"])
        print("ND", data["nondepressive"]["senteceuniquew"])
        
        
        
        print("------------sentece complyexity  - Clauses per statement ---------------")
        print("D", data["depressive"]["clausesperstatement"])
        print("ND", data["nondepressive"]["clausesperstatement"]) 
        
        print("------------lexial density ---------------")
        print("D", data["depressive"]["lexdensity"])
        print("ND", data["nondepressive"]["lexdensity"])
        
        
        print("------------Lexical Sophistication ---------------")
        
        
        print("D", data["depressive"]["lexialspohistication"])
        print("ND", data["nondepressive"]["lexialspohistication"])


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


#print("+++++++++++++++++++++++++++++++++STRAT Processing DONE++++++++++++++++++++++++++++++++++++++++++")    
ProcessLanguageRawfromTranscriptions();
print("+++++++++++++++++++++++++++++++++RAW Processing DONE++++++++++++++++++++++++++++++++++++++++++")
#########DEPRICATED: PostProcessLanguageCreateSummary();
print("+++++++++++++++++++++++++++++++++Create CSV++++++++++++++++++++++++++++++++++++++++++")
CreateCSV();
