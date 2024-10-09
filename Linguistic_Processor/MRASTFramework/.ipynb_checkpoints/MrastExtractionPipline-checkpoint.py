'''
Created on 18. avg. 2022

@author: izido
'''
import csv
import os
import sys
import re 
import json 
import statistics
from pathlib import Path
import matplotlib.pyplot as plt
import pandas as pd

import numpy as np

sys.path.append("language")

from LanguageExtractionPipline import StaznaLanguageExtractionPipline 
from itertools import chain

from pylab import plot, show, savefig, xlim, figure, ylim, legend, boxplot, setp, axes



PATH_TRANSCRIPTIONS = "input/transcriptions/"

ABSOULTIST_GRAMMAR_F = ["absolutely", "all", "always", "complete", "completely", "constant", "constantly", "definitely", "entire", "ever", "every", "everyone", "everything",
                      "full", "must", "never", "nothing", "totally", "whole"]

ABSOULTIST_GRAMMAR_S = ["always", "never", "all the time", "everything", "nothing"]

DEPRESSIVE_GRAMMAR = ["therapy", "psychotherapy", "depression", "depressive", "pills", "medication", "side effects", "hurt", "tears", "alone", "hate", "sleep", "worry"]

map_depressed = ['1002','1003','1004','1006','1007','1008','1009','1010','1011','1012','1013','1014', '1015','1017']

analysis = {};

OUTPUT_FILE = 'data.json';


def LanguageExtractionPipeline():   
    data = load_data(OUTPUT_FILE);
  
    
    print(data)
    slp = StaznaLanguageExtractionPipline()
   

    for id in data:
         
        analysis[id] = {"depressed": 1 if id in map_depressed else 0, "words":0, "sentences":0, "statements":0, "clauses": 0, "sentece_length_data": {"avg": 0.0,"median":0.0,"interquartile":0.0}, "clauses_data": {"avg": 0.0,"median":0.0,"interquartile":0.0}, "lexsophistication_data": {"avg": 0.0,"median":0.0,"interquartile":0.0},
                          "sentece_unique_words_data":{"avg": 0.0,"median":0.0,"interquartile":0.0},
                          "unique_words" : {"count": 0, "percentage" : 0.0 },    
                          "language":{"sentiment": 
                                     {"statement":{"positive":{"count": 0, "percentage" : 0.0}, "negative":{"count": 0, "percentage" : 0.0}, "neutral":{"count": 0, "percentage" : 0.0}}, 
                                      "sentence":{"positive":{"count": 0, "percentage" : 0.0}, "negative":{"count": 0, "percentage" : 0.0}, "neutral":{"count": 0, "percentage" : 0.0}}, 
                                      "word":{"positive":{"count": 0, "percentage" : 0.0}, "negative":{"count": 0, "percentage" : 0.0}, "neutral":{"count": 0, "percentage" : 0.0}}
                                      
                                     },
                                     "clasuses_per_statement":{"avg":[], "map": [], "median": [], "interquartile": []},
                                     "lexialspohistication_per_statement":{"avg":[], "map": [], "median": [], "interquartile": [], "mapdetail":[]},
                                     "sentece_length": {"avg":[], "map": [], "median": [], "interquartile": []},
                                     "sentece_unique_words": {"avg":[], "map": [], "median": [], "interquartile": []},
                                     "pronoun_fps": {"count": 0, "percentage" : 0.0},
                                     "absolutist_words_small": {"count": 0, "percentage" : 0.0},
                                     "absolutist_words_full": {"count": 0, "percentage" : 0.0},
                                     "depression_specific_words": {"count": 0, "percentage" : 0.0},
                                     "pastwords": {"count": 0, "percentage" : 0.0}
                                     }
                          }
        
        with open(data[id]["transcription"], encoding='utf-8-sig') as file:
            lines = file.readlines()
            lines = [line.strip('"').strip() for line in lines]
        
        sentiments = []
        pronouns = []
        pastwords = []
        clauses_avg= [];
        sentece_lenght_avg = [];
        unique_words_per_sentece_avg = [];
        all_unique_words = []
        lexial_sophistication = []
        total_clauses = [];
        #Avg Calculate word length .... word_lengths = [];
        #consider don't as two words using stanza
         #print(lines)
        for l in lines:
            
            #print("line:",l)
            lr = re.sub('\[.[a-z]+\]', "", l)
            #print("line regex:", lr)
            if lr == "" or lr.strip() == "":
                continue;
                   
            words_per_senetce, unique_words_per_sentece, words, word_lengths = slp.LoadData(lr) 
            csr = slp.CaluseRatio();
            total_clauses += csr;
            
            lsop, detail_lsop = slp.LexicalSophistication();
            
            all_unique_words += words;
            
            
            analysis[id]["language"]["clasuses_per_statement"]["map"].append(csr);
            analysis[id]["language"]["clasuses_per_statement"]["avg"].append(round(sum(csr)/len(csr),4)) #0  if len(csr) == 0 else round(sum(csr)/len(csr),4);
            analysis[id]["language"]["clasuses_per_statement"]["median"].append(statistics.median(csr))
            analysis[id]["language"]["clasuses_per_statement"]["interquartile"].append(InterquartileRange(csr));
            
            
                      
            analysis[id]["language"]["sentece_length"]["map"].append(words_per_senetce);
            analysis[id]["language"]["sentece_length"]["avg"].append(round(sum(words_per_senetce)/len(words_per_senetce),4))
            analysis[id]["language"]["sentece_length"]["median"].append(statistics.median(words_per_senetce))
            analysis[id]["language"]["sentece_length"]["interquartile"].append(InterquartileRange(words_per_senetce));
            
            analysis[id]["language"]["sentece_unique_words"]["map"].append(unique_words_per_sentece);
            analysis[id]["language"]["sentece_unique_words"]["avg"].append(round(sum(unique_words_per_sentece)/len(unique_words_per_sentece),4))
            analysis[id]["language"]["sentece_unique_words"]["median"].append(statistics.median(unique_words_per_sentece))
            analysis[id]["language"]["sentece_unique_words"]["interquartile"].append(InterquartileRange(unique_words_per_sentece));
            
            analysis[id]["language"]["lexialspohistication_per_statement"]["mapdetail"].append(detail_lsop);
            analysis[id]["language"]["lexialspohistication_per_statement"]["map"].append(lsop);
            analysis[id]["language"]["lexialspohistication_per_statement"]["avg"].append(round(sum(lsop)/len(lsop),4)) 
            analysis[id]["language"]["lexialspohistication_per_statement"]["median"].append(statistics.median(lsop))
            analysis[id]["language"]["lexialspohistication_per_statement"]["interquartile"].append(InterquartileRange(lsop))
            
            
            clauses_avg.append(round(sum(csr)/len(csr),4));     
            sentece_lenght_avg.append(round(sum(words_per_senetce)/len(words_per_senetce),4));
            unique_words_per_sentece_avg.append(round(sum(unique_words_per_sentece)/len(unique_words_per_sentece),4));   
            lexial_sophistication.append(round(sum(lsop)/len(lsop),4))
           
            sentiments.append(slp.SentimentAnalysis());
            pronouns.append(slp.PronounAnalysis());
            pastwords.append(slp.PastWordsAnalysis());
           
            
            process_specific_words(lr, id, analysis);
           
            print("----Language piline for line done----")    
    
        print("----Language piline for video,",id, " done, Finall statistics...----")   
        
        clasuses_per_statement_map = Arrayto1Darray(analysis[id]["language"]["clasuses_per_statement"]["map"]); 
        lexial_sophistication_per_statement_map = Arrayto1Darray(analysis[id]["language"]["lexialspohistication_per_statement"]["map"]); 
        sentece_unique_words_map = Arrayto1Darray(analysis[id]["language"]["sentece_unique_words"]["map"]);
        sentece_length_map = Arrayto1Darray(analysis[id]["language"]["sentece_length"]["map"]);
        #print("clasuses:", sum(total_clauses), "from map:", sum(clasuses_per_statement_map))
        
        post_process_sentiment(sentiments, id, analysis);
        post_process_firstpersonpronouns(pronouns, id, analysis);
        post_process_pastwords(pastwords, id, analysis);  
        
        analysis[id]["language"]["absolutist_words_full"]["percentage"] = round(analysis[id]["language"]["absolutist_words_full"]["count"]/analysis[id]["words"], 4)
        analysis[id]["language"]["absolutist_words_small"]["percentage"] = round(analysis[id]["language"]["absolutist_words_small"]["count"]/analysis[id]["words"], 4)
        analysis[id]["language"]["depression_specific_words"]["percentage"] = round(analysis[id]["language"]["depression_specific_words"]["count"]/analysis[id]["words"], 4)
        #print ("overall avg", clauses_avg)
       
        #analysis[id]["avg_clause_per_senetce"] = round(sum(clasuses_per_statement_map)/analysis[id]["sentences"],4)
        
        analysis[id]["sentece_length_data"]["avg"] = round(analysis[id]["words"]/analysis[id]["sentences"],4)
        analysis[id]["sentece_length_data"]["median"] = float(statistics.median(sentece_length_map))
        analysis[id]["sentece_length_data"]["interquartile"] = float(InterquartileRange(sentece_length_map))
       
        analysis[id]["sentece_unique_words_data"]["avg"] = float(round(sum(sentece_unique_words_map)/analysis[id]["sentences"],4)) 
        analysis[id]["sentece_unique_words_data"]["median"] = float(statistics.median(sentece_unique_words_map))
        analysis[id]["sentece_unique_words_data"]["interquartile"] = InterquartileRange(sentece_unique_words_map)
        
        analysis[id]["unique_words"]["count"] = len(slp.unique(all_unique_words))
        analysis[id]["unique_words"]["percentage"] = round(analysis[id]["unique_words"]["count"]/analysis[id]["words"],4)
        
        
              
        analysis[id]["lexsophistication_data"]["avg"] = float(round(sum(lexial_sophistication)/len(lexial_sophistication),4))
        analysis[id]["lexsophistication_data"]["median"] = float(statistics.median(lexial_sophistication_per_statement_map))
        analysis[id]["lexsophistication_data"]["interquartile"] = InterquartileRange(lexial_sophistication_per_statement_map)
        
        
        analysis[id]["avg_lexdensity"] = float(round(sum(lexial_sophistication_per_statement_map)/analysis[id]["words"],4))
        
        analysis[id]["clauses"] = int(sum(clasuses_per_statement_map));
        
        analysis[id]["clauses_data"]["avg"] = round( analysis[id]["clauses"]/analysis[id]["sentences"],4)
        analysis[id]["clauses_data"]["median"] = float(statistics.median(clasuses_per_statement_map))
        analysis[id]["clauses_data"]["interquartile"] = InterquartileRange(clasuses_per_statement_map)
        
        #print(analysis[id]);
        
        print("---*************FINALE: " + id + "*****************************----") 
        
        with open("analyssis_video"+str(id)+".json", 'w', encoding='utf-8') as f:
          json.dump(analysis[id], f, ensure_ascii=False, indent=4)
          
    print("---******************************************----")   
    print("Language Analysis done....")    
    print(analysis)
    
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(analysis, f, ensure_ascii=False, indent=4)
        #print(analysis);        
        #exit();     


def InterquartileRange(input):
    data = np.array(input)
    q3, q1 = np.percentile(data, [75 ,25])
    iqr = q3 - q1
    return float(iqr)

def Arrayto1Darray(input):
    flat_list = [item for sublist in input for item in sublist]
    ini_array1 = np.array(flat_list)
    result = ini_array1.flatten()
    #print("**********************")    
    #print(ini_array1)
     
    #print("New resulting array: ", result)
    #print("**********************")    
    return result;


def process_specific_words(line, id, analysis):
    print(line);
    regex_aw_f = '|'.join(ABSOULTIST_GRAMMAR_F).lower()
    regex_aw_s =  '|'.join(ABSOULTIST_GRAMMAR_S).lower()
    regex_dw =  '|'.join(DEPRESSIVE_GRAMMAR).lower()
    
    x = len(re.findall(r'\b(%s)\b' % regex_aw_f, line.lower()))
    y = len(re.findall(r'\b(%s)\b' % regex_aw_s, line.lower()))
    z = len(re.findall(r'\b(%s)\b' % regex_dw, line.lower()))
    
    analysis[id]["language"]["absolutist_words_full"]["count"]+=x
    analysis[id]["language"]["absolutist_words_small"]["count"]+=y
    analysis[id]["language"]["depression_specific_words"]["count"]+=z
    
   # analysis[id]["language"]["pronoun_fps"]["percentage"] = round(analysis[id]["language"]["pronoun_fps"]["count"]/analysis[id]["words"],4)
    #print(x,",", y,"|", analysis[id]["language"]["absolutist_words_full"]["count"],",", analysis[id]["language"]["absolutist_words_small"]["count"])    
def post_process_firstpersonpronouns(data, id, analysis):       
    print(data)
    for d in data:
         analysis[id]["language"]["pronoun_fps"]["count"]+= d
    
    analysis[id]["language"]["pronoun_fps"]["percentage"] = round(analysis[id]["language"]["pronoun_fps"]["count"]/analysis[id]["words"],4)

def post_process_pastwords(data, id, analysis):       
    print(data)
    for d in data:
         analysis[id]["language"]["pastwords"]["count"]+= d
    
    analysis[id]["language"]["pastwords"]["percentage"] = round(analysis[id]["language"]["pastwords"]["count"]/analysis[id]["words"],4)
def post_process_sentiment(data, id, analysis):
    statements = 0;
    words = 0; 
    senteces = 0;
   # print (data)
    for d in data:
         analysis[id]["words"]+=d["word"]["negative"] + d["word"]["positive"] + d["word"]["neutral"]
         analysis[id]["sentences"]+=d["sentence"]["negative"] + d["sentence"]["positive"] + d["sentence"]["neutral"]
         analysis[id]["statements"]+=d["statement"]["negative"] + d["statement"]["positive"] + d["statement"]["neutral"]
        
         analysis[id]["language"]["sentiment"]["statement"]["positive"]["count"] +=  d["statement"]["positive"]
         analysis[id]["language"]["sentiment"]["statement"]["negative"]["count"] +=  d["statement"]["negative"]
         analysis[id]["language"]["sentiment"]["statement"]["neutral"]["count"] +=  d["statement"]["neutral"]
         
         analysis[id]["language"]["sentiment"]["sentence"]["positive"]["count"] +=  d["sentence"]["positive"]
         analysis[id]["language"]["sentiment"]["sentence"]["negative"]["count"] +=  d["sentence"]["negative"]
         analysis[id]["language"]["sentiment"]["sentence"]["neutral"]["count"] +=  d["sentence"]["neutral"]
         
         analysis[id]["language"]["sentiment"]["word"]["positive"]["count"] +=d["word"]["positive"]
         analysis[id]["language"]["sentiment"]["word"]["negative"]["count"] +=d["word"]["negative"]
         analysis[id]["language"]["sentiment"]["word"]["neutral"]["count"] +=d["word"]["neutral"]
         
    
    analysis[id]["language"]["sentiment"]["statement"]["positive"]["percentage"] = round(analysis[id]["language"]["sentiment"]["statement"]["positive"]["count"]/analysis[id]["statements"], 4)
    analysis[id]["language"]["sentiment"]["statement"]["negative"]["percentage"] = round(analysis[id]["language"]["sentiment"]["statement"]["negative"]["count"]/analysis[id]["statements"], 4)
    analysis[id]["language"]["sentiment"]["statement"]["neutral"]["percentage"] = round(analysis[id]["language"]["sentiment"]["statement"]["neutral"]["count"]/analysis[id]["statements"], 4)
    
    analysis[id]["language"]["sentiment"]["sentence"]["positive"]["percentage"] = round(analysis[id]["language"]["sentiment"]["sentence"]["positive"]["count"]/analysis[id]["sentences"], 4)
    analysis[id]["language"]["sentiment"]["sentence"]["negative"]["percentage"] = round(analysis[id]["language"]["sentiment"]["sentence"]["negative"]["count"]/analysis[id]["sentences"], 4)
    analysis[id]["language"]["sentiment"]["sentence"]["neutral"]["percentage"] = round(analysis[id]["language"]["sentiment"]["sentence"]["neutral"]["count"]/analysis[id]["sentences"], 4)
    
    analysis[id]["language"]["sentiment"]["word"]["positive"]["percentage"] = round(analysis[id]["language"]["sentiment"]["word"]["positive"]["count"]/analysis[id]["words"], 4)
    analysis[id]["language"]["sentiment"]["word"]["negative"]["percentage"] = round(analysis[id]["language"]["sentiment"]["word"]["negative"]["count"]/analysis[id]["words"], 4)
    analysis[id]["language"]["sentiment"]["word"]["neutral"]["percentage"] = round(analysis[id]["language"]["sentiment"]["word"]["neutral"]["count"]/analysis[id]["words"], 4)

def load_data(json_output):
     inpudata = {}
     path = Path(json_output);
     if path.is_file():
         os.remove(json_output)
         
     
     for path,dirs,files in os.walk(PATH_TRANSCRIPTIONS):
        for filename in files:
           
            inputtoken = {}
            inputtoken["transcription"] = os.path.join(path,filename)
            video_id = filename.split("_")[0] 
            inpudata[video_id] = inputtoken;
     return inpudata;

def final_data_extraction():
    output = {"depressive": {'total_words': 0, 'total_past_words':0, 'total_first_person_p': 0, 'total_negativ_words': 0, 'total_senteces': 0, 'total_statements': 0, 'total_clauses':0,'total_uniuqe':0,
                            'lexdensity':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                            'lexialspohistication':{"avg": 0.0,"equationm":0.0, "equationst":0.0,  "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "equationraw":[], "values":[], "rawcount": [], "rawlength": []},
                            'pastwords':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                            'pronoun_fps':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                            'absoultistwords':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                            'depressionwords':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                            'clausesperstatement':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                            'sentecelength':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                            'senteceuniquew':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                            'sentecesentiment':{"negative": [], "positive":[], "neutral":[]},
                            'sentecesentimentraw':{"negative": [], "positive":[], "neutral":[]},
                            'wordsentimentraw':{"negative": [], "positive":[], "neutral":[]},
                            'wordsentiment':{"negative": [], "positive":[], "neutral":[]}},
 
              "nondepressive": {'total_words': 0,'total_past_words':0,'total_first_person_p': 0, 'total_negativ_words': 0,'total_senteces': 0, 'total_statements': 0,'total_clauses':0,'total_uniuqe':0,
                                'lexdensity':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                                     'lexialspohistication':{"avg": 0.0, "equationm":0.0, "equationst":0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "equationraw":[], "values":[], "rawcount": [], "rawlength": []},
                                      'pastwords':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                                      'pronoun_fps':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                                       'absoultistwords':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                                       'depressionwords':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                                       'clausesperstatement':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                                        'sentecelength':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                                        'senteceuniquew':{"avg": 0.0, "mean":0.0, "median":0.0, "std": 0.0, "inerquartile": 0.0, "values":[], "rawdata":[]},
                                       'sentecesentiment':{"negative": [], "positive":[], "neutral":[]},
                                       'sentecesentimentraw':{"negative": [], "positive":[], "neutral":[]},
                                      'wordsentimentraw':{"negative": [], "positive":[], "neutral":[]},
                                      'wordsentiment':{"negative": [], "positive":[], "neutral":[]}}}
    avag_words_ps = 0;
    
    f = open('data.json')
    data = json.load(f)
    f.close();
    
    #print(data)
    for id in data:
        video = data [str(id)]
        process = {}
        if video['depressed'] == 1:
            process = output["depressive"] 
        else:
            process = output["nondepressive"] 
            
            
        for entry in list(chain.from_iterable(video["language"]["lexialspohistication_per_statement"]["mapdetail"])):
            process['lexialspohistication']["rawlength"] = process['lexialspohistication']["rawlength"] + entry['lengths']
            
        process['lexialspohistication']["equationraw"].append(round(sum(process['lexialspohistication']["rawlength"])/video['words'],4))     
        #process['lexdensity']["values"].append(video['avg_lexdensity'])
        #process['lexdensity']["values"]=   process['lexdensity']["values"] + video["language"]["clasuses_per_statement"]["avg"]  #list(chain.from_iterable(video["language"]["clasuses_per_statement"]["map"]))
        process['lexdensity']["values"].append(round(sum(list(chain.from_iterable(video["language"]["lexialspohistication_per_statement"]["map"])))*100/video['words'],4))   
        process['lexdensity']["rawdata"].append(sum(list(chain.from_iterable(video["language"]["lexialspohistication_per_statement"]["map"]))))
     
        #process['lexialspohistication']["values"]=   process['lexialspohistication']["values"] + list(chain.from_iterable(video["language"]["lexialspohistication_per_statement"]["map"]))
        process['wordsentiment']['negative'].append(video["language"]["sentiment"]["word"]["negative"]["percentage"])
        process['wordsentiment']['positive'].append(video["language"]["sentiment"]["word"]["positive"]["percentage"])
        process['wordsentiment']['neutral'].append(video["language"]["sentiment"]["word"]["neutral"]["percentage"])
        
        process['wordsentimentraw']['negative'].append(video["language"]["sentiment"]["word"]["negative"]["count"])
        process['wordsentimentraw']['positive'].append(video["language"]["sentiment"]["word"]["positive"]["count"])
        process['wordsentimentraw']['neutral'].append(video["language"]["sentiment"]["word"]["neutral"]["count"])
        
        process['sentecesentiment']['negative'].append(video["language"]["sentiment"]["sentence"]["negative"]["percentage"])
        process['sentecesentiment']['positive'].append(video["language"]["sentiment"]["sentence"]["positive"]["percentage"])
        process['sentecesentiment']['neutral'].append(video["language"]["sentiment"]["sentence"]["neutral"]["percentage"])
        
        process['sentecesentimentraw']['negative'].append(video["language"]["sentiment"]["sentence"]["negative"]["count"])
        process['sentecesentimentraw']['positive'].append(video["language"]["sentiment"]["sentence"]["positive"]["count"])
        process['sentecesentimentraw']['neutral'].append(video["language"]["sentiment"]["sentence"]["neutral"]["count"])
        
        
        
        process['pastwords']["values"].append(video["language"]["pastwords"]["percentage"])
        process['pastwords']["rawdata"].append(video["language"]["pastwords"]["count"])
        process['pronoun_fps']["values"].append(video["language"]["pronoun_fps"]["percentage"])
        process['pronoun_fps']["rawdata"].append(video["language"]["pronoun_fps"]["count"])
        
        
        process['clausesperstatement']["values"] = process['clausesperstatement']["values"] + video["language"]["clasuses_per_statement"]["avg"]
        process['clausesperstatement']["rawdata"] =  process['clausesperstatement']["rawdata"] + list(chain.from_iterable(video["language"]["clasuses_per_statement"]["map"]))
        
        process['sentecelength']["values"] = process['sentecelength']["values"] + video["language"]["sentece_length"]["avg"]
        process['sentecelength']["rawdata"] =  process['sentecelength']["rawdata"] + list(chain.from_iterable(video["language"]["sentece_length"]["map"]))
        
        process['absoultistwords']["values"].append(video["language"]["absolutist_words_full"]["percentage"])
        process['absoultistwords']["rawdata"].append(video["language"]["absolutist_words_full"]["count"])
        
        process['senteceuniquew']["values"] = process['senteceuniquew']["values"] + video["language"]["sentece_unique_words"]["avg"]
        process['senteceuniquew']["rawdata"] =  process['senteceuniquew']["rawdata"] + list(chain.from_iterable(video["language"]["sentece_unique_words"]["map"])) 
        
        
        #process['absoultistwords']["values"].append(video["language"]["absolutist_words_small"]["percentage"])
        #process['absoultistwords']["rawdata"].append(video["language"]["absolutist_words_small"]["count"])
        
        process['depressionwords']["values"].append(video["language"]["depression_specific_words"]["percentage"])
        process['depressionwords']["rawdata"].append(video["language"]["depression_specific_words"]["count"])
        
       
        process['total_past_words'] =  process['total_past_words'] + video["language"]["pastwords"]["count"]
        process['total_first_person_p'] =  process['total_first_person_p'] + video["language"]["pronoun_fps"]["count"]
        process['total_negativ_words'] = process['total_negativ_words'] + video["language"]["sentiment"]["word"]["negative"]["count"]
        
        process['total_words'] =  process['total_words'] + video['words']
        process['total_senteces'] =  process['total_senteces'] + video['sentences']
        process['total_statements'] =  process['total_statements'] + video['statements']
        process['total_clauses'] =  process['total_clauses'] + video['clauses'] 
        process['total_uniuqe'] =  process['total_uniuqe'] + video['unique_words']['count']
        
      
       
        
        
    output["depressive"]['pastwords']["inerquartile"] = InterquartileRange(output["depressive"]['pastwords']["values"])
    output["nondepressive"]['pastwords']["inerquartile"] = InterquartileRange(output["nondepressive"]['pastwords']["values"])
    output["depressive"]['pastwords']["std"] = statistics.stdev(output["depressive"]['pastwords']["values"])
    output["nondepressive"]['pastwords']["std"] = statistics.stdev(output["nondepressive"]['pastwords']["values"])
    output["depressive"]['pastwords']["median"] = statistics.median(output["depressive"]['pastwords']["values"])
    output["nondepressive"]['pastwords']["median"] = statistics.median(output["nondepressive"]['pastwords']["values"])
    output["depressive"]['pastwords']["mean"] = statistics.mean(output["depressive"]['pastwords']["values"])
    output["nondepressive"]['pastwords']["mean"] = statistics.mean(output["nondepressive"]['pastwords']["values"])
    
    output["depressive"]['absoultistwords']["inerquartile"] = InterquartileRange(output["depressive"]['absoultistwords']["values"])
    output["nondepressive"]['absoultistwords']["inerquartile"] = InterquartileRange(output["nondepressive"]['absoultistwords']["values"])
    output["depressive"]['absoultistwords']["std"] = statistics.stdev(output["depressive"]['absoultistwords']["values"])
    output["nondepressive"]['absoultistwords']["std"] = statistics.stdev(output["nondepressive"]['absoultistwords']["values"])
    output["depressive"]['absoultistwords']["median"] = statistics.median(output["depressive"]['absoultistwords']["values"])
    output["nondepressive"]['absoultistwords']["median"] = statistics.median(output["nondepressive"]['absoultistwords']["values"])
    output["depressive"]['absoultistwords']["mean"] = statistics.mean(output["depressive"]['absoultistwords']["values"])
    output["nondepressive"]['absoultistwords']["mean"] = statistics.mean(output["nondepressive"]['absoultistwords']["values"])
    
    output["depressive"]['depressionwords']["inerquartile"] = InterquartileRange(output["depressive"]['depressionwords']["values"])
    output["nondepressive"]['depressionwords']["inerquartile"] = InterquartileRange(output["nondepressive"]['depressionwords']["values"])
    output["depressive"]['depressionwords']["std"] = statistics.stdev(output["depressive"]['depressionwords']["values"])
    output["nondepressive"]['depressionwords']["std"] = statistics.stdev(output["nondepressive"]['depressionwords']["values"])
    output["depressive"]['depressionwords']["median"] = statistics.median(output["depressive"]['depressionwords']["values"])
    output["nondepressive"]['depressionwords']["median"] = statistics.median(output["nondepressive"]['depressionwords']["values"])
    output["depressive"]['depressionwords']["mean"] = statistics.mean(output["depressive"]['depressionwords']["values"])
    output["nondepressive"]['depressionwords']["mean"] = statistics.mean(output["nondepressive"]['depressionwords']["values"])
    
    
    output["depressive"]['pronoun_fps']["inerquartile"] = InterquartileRange(output["depressive"]['pronoun_fps']["values"])
    output["nondepressive"]['pronoun_fps']["inerquartile"] = InterquartileRange(output["nondepressive"]['pronoun_fps']["values"])
    output["depressive"]['pronoun_fps']["std"] = statistics.stdev(output["depressive"]['pronoun_fps']["values"])
    output["nondepressive"]['pronoun_fps']["std"] = statistics.stdev(output["nondepressive"]['pronoun_fps']["values"])
    output["depressive"]['pronoun_fps']["median"] = statistics.median(output["depressive"]['pronoun_fps']["values"])
    output["nondepressive"]['pronoun_fps']["median"] = statistics.median(output["nondepressive"]['pronoun_fps']["values"])
    output["depressive"]['pronoun_fps']["mean"] = statistics.mean(output["depressive"]['pronoun_fps']["values"])
    output["nondepressive"]['pronoun_fps']["mean"] = statistics.mean(output["nondepressive"]['pronoun_fps']["values"])
    
        
    output["depressive"]['lexdensity']["inerquartile"] = InterquartileRange(output["depressive"]['lexdensity']["values"])
    output["nondepressive"]['lexdensity']["inerquartile"] = InterquartileRange(output["nondepressive"]['lexdensity']["values"])
    output["depressive"]['lexdensity']["std"] = statistics.stdev(output["depressive"]['lexdensity']["values"])
    output["nondepressive"]['lexdensity']["std"] = statistics.stdev(output["nondepressive"]['lexdensity']["values"])
    output["depressive"]['lexdensity']["median"] = statistics.median(output["depressive"]['lexdensity']["values"])
    output["nondepressive"]['lexdensity']["median"] = statistics.median(output["nondepressive"]['lexdensity']["values"])
    output["depressive"]['lexdensity']["mean"] = statistics.mean(output["depressive"]['lexdensity']["values"])
    output["nondepressive"]['lexdensity']["mean"] = statistics.mean(output["nondepressive"]['lexdensity']["values"])
    
    #output["depressive"]['lexdensity']["avg"] = float(round(sum(output["depressive"]['lexdensity']["values"])/len(output["depressive"]['lexdensity']["values"]),4))
    #output["nondepressive"]['lexdensity']["avg"] = float(round(sum(output["nondepressive"]['lexdensity']["values"])/len(output["nondepressive"]['lexdensity']["values"]),4))
    
     #process['wordsentiment']['negative']
    ###Sophistication
    
    output["depressive"]['clausesperstatement']["inerquartile"] = InterquartileRange(output["depressive"]['clausesperstatement']["values"])
    output["nondepressive"]['clausesperstatement']["inerquartile"] = InterquartileRange(output["nondepressive"]['clausesperstatement']["values"])
    output["depressive"]['clausesperstatement']["std"] = statistics.stdev(output["depressive"]['clausesperstatement']["values"])
    output["nondepressive"]['clausesperstatement']["std"] = statistics.stdev(output["nondepressive"]['clausesperstatement']["values"])
    output["depressive"]['clausesperstatement']["median"] = statistics.median(output["depressive"]['clausesperstatement']["values"])
    output["nondepressive"]['clausesperstatement']["median"] = statistics.median(output["nondepressive"]['clausesperstatement']["values"])
    output["depressive"]['clausesperstatement']["mean"] = statistics.mean(output["depressive"]['clausesperstatement']["values"])
    output["nondepressive"]['clausesperstatement']["mean"] = statistics.mean(output["nondepressive"]['clausesperstatement']["values"])
    
    
    output["depressive"]['sentecelength']["inerquartile"] = InterquartileRange(output["depressive"]['sentecelength']["values"])
    output["nondepressive"]['sentecelength']["inerquartile"] = InterquartileRange(output["nondepressive"]['sentecelength']["values"])
    output["depressive"]['sentecelength']["std"] = statistics.stdev(output["depressive"]['sentecelength']["values"])
    output["nondepressive"]['sentecelength']["std"] = statistics.stdev(output["nondepressive"]['sentecelength']["values"])
    output["depressive"]['sentecelength']["median"] = statistics.median(output["depressive"]['sentecelength']["values"])
    output["nondepressive"]['sentecelength']["median"] = statistics.median(output["nondepressive"]['sentecelength']["values"])
    output["depressive"]['sentecelength']["mean"] = statistics.mean(output["depressive"]['sentecelength']["values"])
    output["nondepressive"]['sentecelength']["mean"] = statistics.mean(output["nondepressive"]['sentecelength']["values"])
    
    output["depressive"]['senteceuniquew']["inerquartile"] = InterquartileRange(output["depressive"]['senteceuniquew']["values"])
    output["nondepressive"]['senteceuniquew']["inerquartile"] = InterquartileRange(output["nondepressive"]['senteceuniquew']["values"])
    output["depressive"]['senteceuniquew']["std"] = statistics.stdev(output["depressive"]['senteceuniquew']["values"])
    output["nondepressive"]['senteceuniquew']["std"] = statistics.stdev(output["nondepressive"]['senteceuniquew']["values"])
    output["depressive"]['senteceuniquew']["median"] = statistics.median(output["depressive"]['senteceuniquew']["values"])
    output["nondepressive"]['senteceuniquew']["median"] = statistics.median(output["nondepressive"]['senteceuniquew']["values"])
    output["depressive"]['senteceuniquew']["mean"] = statistics.mean(output["depressive"]['senteceuniquew']["values"])
    output["nondepressive"]['senteceuniquew']["mean"] = statistics.mean(output["nondepressive"]['senteceuniquew']["values"])
    
 
    
    
    output["depressive"]['lexialspohistication']["inerquartile"] = InterquartileRange(output["depressive"]['lexialspohistication']["rawlength"])
    output["nondepressive"]['lexialspohistication']["inerquartile"] = InterquartileRange(output["nondepressive"]['lexialspohistication']["rawlength"])
    output["depressive"]['lexialspohistication']["std"] = statistics.stdev(output["depressive"]['lexialspohistication']["rawlength"])
    output["nondepressive"]['lexialspohistication']["std"] = statistics.stdev(output["nondepressive"]['lexialspohistication']["rawlength"])
    output["depressive"]['lexialspohistication']["median"] = statistics.median(output["depressive"]['lexialspohistication']["rawlength"])
    output["nondepressive"]['lexialspohistication']["median"] = statistics.median(output["nondepressive"]['lexialspohistication']["rawlength"])
    output["depressive"]['lexialspohistication']["mean"] = statistics.mean(output["depressive"]['lexialspohistication']["rawlength"])
    output["nondepressive"]['lexialspohistication']["mean"] = statistics.mean(output["nondepressive"]['lexialspohistication']["rawlength"])
    
    output["depressive"]['lexialspohistication']["equationst"] = statistics.stdev(output["depressive"]['lexialspohistication']["equationraw"])
    output["nondepressive"]['lexialspohistication']["equationst"] = statistics.stdev(output["nondepressive"]['lexialspohistication']["equationraw"])
    output["depressive"]['lexialspohistication']["equationm"] = statistics.median(output["depressive"]['lexialspohistication']["equationraw"])
    output["nondepressive"]['lexialspohistication']["equationm"] = statistics.median(output["nondepressive"]['lexialspohistication']["equationraw"])
    
    
    #output["depressive"]['lexialspohistication']["avg"] = float(round(sum(output["depressive"]['lexialspohistication']["values"])/len(output["depressive"]['lexialspohistication']["values"]),4))
    #output["nondepressive"]['lexialspohistication']["avg"] = float(round(sum(output["nondepressive"]['lexialspohistication']["values"])/len(output["nondepressive"]['lexialspohistication']["values"]),4))
  
    output["depressive"]["overallws"] = {"positive": {"mean":statistics.mean( output["depressive"]['wordsentiment']['positive']), "median":statistics.median( output["depressive"]['wordsentiment']['positive']), "std":statistics.stdev( output["depressive"]['wordsentiment']['positive']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['positive'])},
                                         "negative": {"mean":statistics.mean( output["depressive"]['wordsentiment']['negative']), "median":statistics.median( output["depressive"]['wordsentiment']['negative']), "std":statistics.stdev( output["depressive"]['wordsentiment']['negative']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['negative'])},
                                         "neutral" : {"mean":statistics.mean( output["depressive"]['wordsentiment']['neutral']), "median":statistics.median( output["depressive"]['wordsentiment']['neutral']), "std":statistics.stdev( output["depressive"]['wordsentiment']['neutral']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['neutral'])}}
    
    output["nondepressive"]["overallws"] = {"positive": {"mean":statistics.mean( output["nondepressive"]['wordsentiment']['positive']), "median":statistics.median( output["nondepressive"]['wordsentiment']['positive']), "std":statistics.stdev( output["nondepressive"]['wordsentiment']['positive']), "inerquartile":InterquartileRange(output["nondepressive"]['wordsentiment']['positive'])},
                                         "negative": {"mean":statistics.mean( output["nondepressive"]['wordsentiment']['negative']), "median":statistics.median( output["nondepressive"]['wordsentiment']['negative']), "std":statistics.stdev( output["nondepressive"]['wordsentiment']['negative']), "inerquartile":InterquartileRange(output["nondepressive"]['wordsentiment']['negative'])},
                                         "neutral" : {"mean":statistics.mean( output["nondepressive"]['wordsentiment']['neutral']), "median":statistics.median( output["nondepressive"]['wordsentiment']['neutral']), "std":statistics.stdev( output["nondepressive"]['wordsentiment']['neutral']), "inerquartile":InterquartileRange(output["nondepressive"]['wordsentiment']['neutral'])}}
   
    
    output["depressive"]["overallsens"] = {"positive": {"mean":statistics.mean( output["depressive"]['sentecesentiment']['positive']), "median":statistics.median( output["depressive"]['wordsentiment']['positive']), "std":statistics.stdev( output["depressive"]['wordsentiment']['positive']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['positive'])},
                                         "negative": {"mean":statistics.mean( output["depressive"]['sentecesentiment']['negative']), "median":statistics.median( output["depressive"]['wordsentiment']['negative']), "std":statistics.stdev( output["depressive"]['wordsentiment']['negative']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['negative'])},
                                         "neutral" : {"mean":statistics.mean( output["depressive"]['sentecesentiment']['neutral']), "median":statistics.median( output["depressive"]['wordsentiment']['neutral']), "std":statistics.stdev( output["depressive"]['wordsentiment']['neutral']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['neutral'])}}
    
    output["nondepressive"]["overallsens"] = {"positive": {"mean":statistics.mean( output["nondepressive"]['sentecesentiment']['positive']), "median":statistics.median( output["nondepressive"]['sentecesentiment']['positive']), "std":statistics.stdev( output["nondepressive"]['sentecesentiment']['positive']), "inerquartile":InterquartileRange(output["nondepressive"]['sentecesentiment']['positive'])},
                                         "negative": {"mean":statistics.mean( output["nondepressive"]['sentecesentiment']['negative']), "median":statistics.median( output["nondepressive"]['sentecesentiment']['negative']), "std":statistics.stdev( output["nondepressive"]['sentecesentiment']['negative']), "inerquartile":InterquartileRange(output["nondepressive"]['sentecesentiment']['negative'])},
                                         "neutral" : {"mean":statistics.mean( output["nondepressive"]['sentecesentiment']['neutral']), "median":statistics.median( output["nondepressive"]['sentecesentiment']['neutral']), "std":statistics.stdev( output["nondepressive"]['sentecesentiment']['neutral']), "inerquartile":InterquartileRange(output["nondepressive"]['sentecesentiment']['neutral'])}}
    
        
    return output;

def setBoxColors(bp):
    setp(bp['boxes'][0], color='blue')
    setp(bp['caps'][0], color='blue')
    setp(bp['caps'][1], color='blue')
    setp(bp['whiskers'][0], color='blue')
    setp(bp['whiskers'][1], color='blue')
    setp(bp['fliers'][0], color='blue')
    setp(bp['fliers'][1], color='blue')
    setp(bp['medians'][0], color='blue')

    setp(bp['boxes'][1], color='red')
    setp(bp['caps'][2], color='red')
    setp(bp['caps'][3], color='red')
    setp(bp['whiskers'][2], color='red')
    setp(bp['whiskers'][3], color='red')
    setp(bp['fliers'][2], color='red')
    setp(bp['fliers'][3], color='red')
    setp(bp['medians'][1], color='red')



# main();
# exit()

data = final_data_extraction();  

# d_rawpastwords_standardized =  [round((x/data["depressive"]["total_words"])*1000,2) for x in data["depressive"]['pastwords']["rawdata"]]
# nd_rawpastwords_standardized =  [round((x/data["nondepressive"]["total_words"])*1000,2) for x in data["nondepressive"]['pastwords']["rawdata"]]
# print("total_words D:", data["depressive"]["total_words"])
# print("total_words ND:", data["nondepressive"]["total_words"])

ngw_depressive = sum(data["depressive"]['wordsentimentraw']['negative']);
ngw_ndepressive = sum(data["nondepressive"]['wordsentimentraw']['negative']);

nuw_depressive = sum(data["depressive"]['wordsentimentraw']['neutral']);
nuw_ndepressive = sum(data["nondepressive"]['wordsentimentraw']['neutral']);

pw_depressive = sum(data["depressive"]['wordsentimentraw']['positive']);
pw_ndepressive = sum(data["nondepressive"]['wordsentimentraw']['positive']);

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


# print("------------lexial density ---------------")
# print("D", data["depressive"]["lexdensity"])
# print("ND", data["nondepressive"]["lexdensity"])


# print("------------Lexical Sophistication ---------------")


# print("D", data["depressive"]["lexialspohistication"])
# print("equation:", )
# print("ND", data["nondepressive"]["lexialspohistication"])



boxplotlexdensity = {"depressive": [], "nondepressive": []}      
boxplotlexdensity["depressive"] = data["depressive"]['lexdensity']["values"]
boxplotlexdensity["nondepressive"] = data["nondepressive"]['lexdensity']["values"]


    #    fig = plt.figure(figsize =(10, 7))

#df = pd.DataFrame(data["depressive"]["overallws"][""])
#df = pd.DataFrame({"D": data["depressive"]["overallws"]["negative"],
                  # "ND": data["nondepressive"]["overallws"]["negative"]})


A = []
A.append(data["depressive"]['wordsentiment']['positive'])
A.append(data["depressive"]["wordsentiment"]["negative"])
A.append(data["depressive"]["wordsentiment"]["neutral"])

B = []
B.append(data["nondepressive"]['wordsentiment']['positive'])
B.append(data["nondepressive"]["wordsentiment"]["negative"])
B.append(data["nondepressive"]["wordsentiment"]["neutral"])



# df = pd.DataFrame(A)

# plt.boxplot(df)# show plot
# plt.show()  
# main();
