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

from language.LanguageExtractionPipline import StaznaLanguageExtractionPipline

from itertools import chain

from pylab import plot, show, savefig, xlim, figure, ylim, legend, boxplot, setp, axes



PATH_TRANSCRIPTIONS = "input/transcriptions/"
PATH_RESULTS = "output/" 

ABSOULTIST_GRAMMAR_F = ["absolutely", "all", "always", "complete", "completely", "constant", "constantly", "definitely", "entire", "ever", "every", "everyone", "everything",
                      "full", "must", "never", "nothing", "totally", "whole"]

ABSOULTIST_GRAMMAR_S = ["always", "never", "all the time", "everything", "nothing"]

DEPRESSIVE_GRAMMAR = ["therapy", "psychotherapy", "depression", "depressive", "pills", "medication", "side effects", "hurt", "tears", "alone", "hate", "sleep", "worry"]




# #######DAICWOZ CONFIG############################################
# MAP_DEPRESSED = ['321','338','345','347','348','351','362','414','426','441','448','459' ] ####DAICWOZ

########SYMPTOMMEDIA CONFIG############################################
MAP_DEPRESSED = ['1002','1003','1004','1006','1007','1008','1009','1010','1011','1012','1013','1014', '1015','1017',"10181"] ####SYMPTOMMEDIA

class LanguageProcessor:
    
    def __init__(self):
        self.slp = StaznaLanguageExtractionPipline()
        
    #def ProcessTranscription(self, lines = [], sequence_id = "0000"):
    def ProcessTranscription(self, asr_text_result, sequence_id = "0000"):
        
                
        analysis = {"depressed": 1 if sequence_id in MAP_DEPRESSED else 0, "words":0, "sentences":0, "statements":0, "clauses": 0, "sentece_length_data": {"avg": 0.0,"median":0.0,"interquartile":0.0}, "clauses_data": {"avg": 0.0,"median":0.0,"interquartile":0.0}, "lexsophistication_data": {"avg": 0.0,"median":0.0,"interquartile":0.0},
                          "sentece_unique_words_data":{"avg": 0.0,"median":0.0,"interquartile":0.0},
                          "unique_words" : {"count": 0, "percentage" : 0.0 },    
                          "language":{"sentiment": 
                                     {"statement":{"positive":{"count": 0, "percentage" : 0.0}, "negative":{"count": 0, "percentage" : 0.0}, "neutral":{"count": 0, "percentage" : 0.0}}, 
                                      "sentence":{"positive":{"count": 0, "percentage" : 0.0}, "negative":{"count": 0, "percentage" : 0.0}, "neutral":{"count": 0, "percentage" : 0.0}}, 
                                      "word":{"positive":{"count": 0, "percentage" : 0.0}, "negative":{"count": 0, "percentage" : 0.0}, "neutral":{"count": 0, "percentage" : 0.0}}
                                      
                                     },
                                     "clasuses_per_statement":{"avg":[], "map": [], "median": [], "interquartile": []},
                                     "lexialspohistication_per_statement":{"avg":[], "map": [], "median": [], "interquartile": [], "mapdetail":[]},
                                     "lexial_density_per_sentece":[],"lexial_density_per_sentece_nofeats": [],
                                     "sentece_length": {"avg":[], "map": [], "median": [], "interquartile": []},
                                     "sentece_unique_words": {"avg":[], "map": [], "median": [], "interquartile": []},
                                     "pronoun_fps": {"count": 0, "percentage" : 0.0},
                                     "absolutist_words_small": {"count": 0, "percentage" : 0.0},
                                     "absolutist_words_full": {"count": 0, "percentage" : 0.0},
                                     "depression_specific_words": {"count": 0, "percentage" : 0.0},
                                     "pastwords": {"count": 0, "percentage" : 0.0}
                                     }
                          }
        
        sentiments = []
        pronouns = []
        pastwords = []
        clauses_avg= [];
        sentece_lenght_avg = [];
        unique_words_per_sentece_avg = [];
        all_unique_words = []
        lexial_sophistication = []
        total_clauses = [];
        
        #for l in lines:
            
        #lr = re.sub('\[.[a-z]+\]', "", l)
        lr = re.sub('\[.[a-z]+\]', "", asr_text_result)
        
        #if lr == "" or lr.strip() == "":
            #continue;
        
        print("--------------",lr,"-----------------")       
        
        words_per_senetce, unique_words_per_sentece, words, word_lengths = self.slp.LoadData(lr) 
        csr = self.slp.CaluseRatio();
        total_clauses += csr;
            
        lsop, detail_lsop, density_map_per_senece, density_map_per_senece_nofeats = self.slp.LexicalSophisticationAndDensity();
            
        if len(lsop) > 0 and len(detail_lsop) > 0: 
            
            all_unique_words += words;
                
                
            analysis["language"]["clasuses_per_statement"]["map"].append(csr);
            analysis["language"]["clasuses_per_statement"]["avg"].append(round(sum(csr)/len(csr),4)) #0  if len(csr) == 0 else round(sum(csr)/len(csr),4);
            analysis["language"]["clasuses_per_statement"]["median"].append(statistics.median(csr))
            analysis["language"]["clasuses_per_statement"]["interquartile"].append(InterquartileRange(csr));                              
                          
            analysis["language"]["sentece_length"]["map"].append(words_per_senetce);
            analysis["language"]["sentece_length"]["avg"].append(round(sum(words_per_senetce)/len(words_per_senetce),4))
            analysis["language"]["sentece_length"]["median"].append(statistics.median(words_per_senetce))
            analysis["language"]["sentece_length"]["interquartile"].append(InterquartileRange(words_per_senetce));
                
            analysis["language"]["sentece_unique_words"]["map"].append(unique_words_per_sentece);
            analysis["language"]["sentece_unique_words"]["avg"].append(round(sum(unique_words_per_sentece)/len(unique_words_per_sentece),4))
            analysis["language"]["sentece_unique_words"]["median"].append(statistics.median(unique_words_per_sentece))
            analysis["language"]["sentece_unique_words"]["interquartile"].append(InterquartileRange(unique_words_per_sentece));
                
            analysis["language"]["lexialspohistication_per_statement"]["mapdetail"].append(detail_lsop);
            analysis["language"]["lexialspohistication_per_statement"]["map"].append(lsop);
            analysis["language"]["lexialspohistication_per_statement"]["avg"].append(round(sum(lsop)/len(lsop),4)) 
            analysis["language"]["lexialspohistication_per_statement"]["median"].append(statistics.median(lsop))
            analysis["language"]["lexialspohistication_per_statement"]["interquartile"].append(InterquartileRange(lsop))
                
            analysis["language"]["lexial_density_per_sentece"].append(density_map_per_senece);
            analysis["language"]["lexial_density_per_sentece_nofeats"].append(density_map_per_senece_nofeats);
                
    
            clauses_avg.append(round(sum(csr)/len(csr),4));     
            sentece_lenght_avg.append(round(sum(words_per_senetce)/len(words_per_senetce),4));
            unique_words_per_sentece_avg.append(round(sum(unique_words_per_sentece)/len(unique_words_per_sentece),4));   
            lexial_sophistication.append(round(sum(lsop)/len(lsop),4))
               
            sentiments.append(self.slp.SentimentAnalysis());
            pronouns.append(self.slp.PronounAnalysis());
            pastwords.append(self.slp.PastWordsAnalysis());               
                
            process_specific_words(lr,analysis);
               
            #print("----Language piline for line done----")    
    
        print("----Language piline for video,",id, " done, Finall statistics...----")   
        
        clasuses_per_statement_map = Arrayto1Darray(analysis["language"]["clasuses_per_statement"]["map"]); 
        lexial_sophistication_per_statement_map = Arrayto1Darray(analysis["language"]["lexialspohistication_per_statement"]["map"]); 
        sentece_unique_words_map = Arrayto1Darray(analysis["language"]["sentece_unique_words"]["map"]);
        sentece_length_map = Arrayto1Darray(analysis["language"]["sentece_length"]["map"]);
        #print("clasuses:", sum(total_clauses), "from map:", sum(clasuses_per_statement_map))
        
        post_process_sentiment(sentiments, analysis);
        post_process_firstpersonpronouns(pronouns, analysis);
        post_process_pastwords(pastwords, analysis);  
        
        analysis["language"]["absolutist_words_full"]["percentage"] = round(analysis["language"]["absolutist_words_full"]["count"]/analysis["words"], 4)
        analysis["language"]["absolutist_words_small"]["percentage"] = round(analysis["language"]["absolutist_words_small"]["count"]/analysis["words"], 4)
        analysis["language"]["depression_specific_words"]["percentage"] = round(analysis["language"]["depression_specific_words"]["count"]/analysis["words"], 4)
        #print ("overall avg", clauses_avg)
       
        #analysis["avg_clause_per_senetce"] = round(sum(clasuses_per_statement_map)/analysis["sentences"],4)
        
        analysis["sentece_length_data"]["avg"] = round(analysis["words"]/analysis["sentences"],4)
        analysis["sentece_length_data"]["median"] = float(statistics.median(sentece_length_map))
        analysis["sentece_length_data"]["interquartile"] = float(InterquartileRange(sentece_length_map))
       
        analysis["sentece_unique_words_data"]["avg"] = float(round(sum(sentece_unique_words_map)/analysis["sentences"],4)) 
        analysis["sentece_unique_words_data"]["median"] = float(statistics.median(sentece_unique_words_map))
        analysis["sentece_unique_words_data"]["interquartile"] = InterquartileRange(sentece_unique_words_map)
        
        analysis["unique_words"]["count"] = len(self.slp.unique(all_unique_words))
        analysis["unique_words"]["percentage"] = round(analysis["unique_words"]["count"]/analysis["words"],4)
                             
        analysis["lexsophistication_data"]["avg"] = float(round(sum(lexial_sophistication)/len(lexial_sophistication),4))
        analysis["lexsophistication_data"]["median"] = float(statistics.median(lexial_sophistication_per_statement_map))
        analysis["lexsophistication_data"]["interquartile"] = InterquartileRange(lexial_sophistication_per_statement_map)
                        
        analysis["clauses"] = int(sum(clasuses_per_statement_map));
        
        analysis["clauses_data"]["avg"] = round( analysis["clauses"]/analysis["sentences"],4)
        analysis["clauses_data"]["median"] = float(statistics.median(clasuses_per_statement_map))
        analysis["clauses_data"]["interquartile"] = InterquartileRange(clasuses_per_statement_map)
        
        ##Density per sentece (this is avg average per each sentece) and per video (merge and create all unique types and calculate TTR),
        ## analysis["language"]["lexial_density_per_sentece"].append(density_map_per_senece);
        ##  analysis["language"]["lexial_density_per_sentece_nofeats"].append(density_map_per_senece_nofeats);
        
        D_density = list(chain.from_iterable(analysis["language"]["lexial_density_per_sentece"]))
        D_density_nofeats = list(chain.from_iterable(analysis["language"]["lexial_density_per_sentece_nofeats"]))
        
        print("########........Token#########################")
        print(D_density)
        print("########.......Token#########################")
        print(D_density_nofeats)
        
        #print(D_density);
        average_densities_per_senteces = [item["value"] for item in D_density]
        average_densities_per_senteces_nofeats = [item["value"] for item in D_density_nofeats]
        
        analysis["avg_lexdensity_per_specific_POS"] = float(round(sum(lexial_sophistication_per_statement_map)/analysis["words"],4)) #limit to self.LEXIAL_SOPHISTICATION_UPOS = ["ADJ", "ADV", "NOUN", "VERB", "PROPN"] 
        analysis["avg_lexdensity_senetece"] = round(statistics.mean(average_densities_per_senteces),4) #use feats and do average based on inner sentece 
        analysis["avg_lexdensity_senetece_nofeats"] = round(statistics.mean(average_densities_per_senteces_nofeats),4)
        
        wtypes =  [wt["typesmap"] for wt in D_density]
        list_set =  list(chain.from_iterable(wtypes))
        
        wtypes_nofeat =  [wtnf["typesmap"] for wtnf in D_density_nofeats]
        list_set_nofeats = list(chain.from_iterable(wtypes_nofeat))
        #print("*******************************************")
        #print("+++++++++++++++++++++++++++++++++++++++++++++++")
        #print(list_set)
        #print("+++++++++++++++++++++++++++++++++++++++++++++++")
       # print(list_set_nofeats)
       # print("***********************************************")
        #exit()
        
        unquie_word_types =  list(set(list_set)) 
        unquie_word_types_nofeats =  list(set(list_set_nofeats)) 
        
        print("+++++++++++++++++++++++++++++++++++++++++++++++")
        print(unquie_word_types)
       # print("+++++++++++++++++++++++++++++++++++++++++++++++")
        print(unquie_word_types_nofeats)
        print("***********************************************")
       # exit()
        
        analysis["avg_lexdensity_video"] = float(len(unquie_word_types)/analysis["words"]) #use feats and calculate based on the whole statement. 
        analysis["avg_lexdensity_video_nofeats"] = float(len(unquie_word_types_nofeats)/analysis["words"]) #use feats and calculate based on the whole statement. 
        
        analysis["avg_lexdensity_video_normalized"] =   float(analysis["words"] * analysis["avg_lexdensity_video"] / 100) 
        analysis["avg_lexdensity_video_nofeats_normalized"] =  float(analysis["words"]  * analysis["avg_lexdensity_video_nofeats"] / 100)
        
        
        #print(unquie_word_types)
        #print(unquie_word_types_nofeats)
        #print(analysis["avg_lexdensity_senetece"], analysis["avg_lexdensity_video"],":NOFEATS:", analysis["avg_lexdensity_senetece_nofeats"], analysis["avg_lexdensity_video_nofeats"])
        #exit()
        #analysis["avg_lexial_density_per_sentece"] = ;
        
        #print(analysis);
        
        #print("---*************FINALE: " + id + "*****************************----") 
        #
        #with open("analyssis_video"+str(id)+".json", 'w', encoding='utf-8') as f:
        #  json.dump(analysis, f, ensure_ascii=False, indent=4)
        return  analysis
    
    def PostProcessCreateSummary(self,analysissource):         
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
                                'wordsentiment':{"negative": [], "positive":[], "neutral":[]},
                                'lexdensity_tokens': {"values":[],"avg": 0.0, "std": 0.0, "ntokens": 0},
                                'lexdensity_senetce': {"values":[],"avg": 0.0, "std": 0.0}, 
                                'lexdensity_video': {"values":[],"avg": 0.0, "std": 0.0},  
                                'lexdensity_senetce_nofeats': {"values":[],"avg": 0.0, "std": 0.0}, 
                                'lexdensity_video_nofeats': {"values":[],"avg": 0.0, "std": 0.0},
                                'avg_lexdensity_video_normalized': {"values":[],"avg": 0.0, "std": 0.0},
                                'avg_lexdensity_video_nofeats_normalized': {"values":[],"avg": 0.0, "std": 0.0}
                    },
     
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
                                          'wordsentiment':{"negative": [], "positive":[], "neutral":[]},
                                          'lexdensity_tokens': {"values":[],"avg": 0.0, "std": 0.0, "ntokens": 0},
                                          'lexdensity_senetce': {"values":[],"avg": 0.0, "std": 0.0}, 
                                          'lexdensity_video': {"values":[],"avg": 0.0, "std": 0.0},  
                                          'lexdensity_senetce_nofeats': {"values":[],"avg": 0.0, "std": 0.0}, 
                                          'lexdensity_video_nofeats': {"values":[],"avg": 0.0, "std": 0.0},
                                          'avg_lexdensity_video_normalized': {"values":[],"avg": 0.0, "std": 0.0},
                                          'avg_lexdensity_video_nofeats_normalized': {"values":[],"avg": 0.0, "std": 0.0}  
                        }
                  
                    }
        avag_words_ps = 0; 
        
        f = open(analysissource)
        data = json.load(f)
        f.close();
        
        hasDepressed = False;
        hasnonDepressed = False;
        
        for id in data:
            video = data [str(id)]
            print(video)
            process = {}
            if video['depressed'] == 1:
                process = output["depressive"] 
                hasDepressed = True
            else:
                process = output["nondepressive"] 
                hasnonDepressed = True
                
                
            for entry in list(chain.from_iterable(video["language"]["lexialspohistication_per_statement"]["mapdetail"])):
                process['lexialspohistication']["rawlength"] = process['lexialspohistication']["rawlength"] + entry['lengths']
            
            process['lexialspohistication']["equationraw"].append(round(sum(process['lexialspohistication']["rawlength"])/video['words'],4))     

            
            process['lexdensity_tokens']["values"].append(video['avg_lexdensity_per_specific_POS'])
            
            process['lexdensity_tokens']["ntokens"] = video['words'];
            #process['lexdensity']["values"]=   process['lexdensity']["values"] + video["language"]["clasuses_per_statement"]["avg"]  #list(chain.from_iterable(video["language"]["clasuses_per_statement"]["map"]))
            process['lexdensity']["values"].append(round(sum(list(chain.from_iterable(video["language"]["lexialspohistication_per_statement"]["map"])))*100/video['words'],4))   
            process['lexdensity']["rawdata"].append(sum(list(chain.from_iterable(video["language"]["lexialspohistication_per_statement"]["map"]))))
            
            
            process['lexdensity_senetce']["values"].append(video['avg_lexdensity_senetece'])
            process['lexdensity_video']["values"].append(video['avg_lexdensity_video'])
            process['lexdensity_senetce_nofeats']["values"].append(video['avg_lexdensity_senetece_nofeats'])
            process['lexdensity_video_nofeats']["values"].append(video['avg_lexdensity_video_nofeats'])
            
            process['avg_lexdensity_video_normalized']["values"].append(video['avg_lexdensity_video_normalized'])
            process['avg_lexdensity_video_nofeats_normalized']["values"].append(video['avg_lexdensity_video_nofeats_normalized'])
            
            #    v =  output["depressive"]['pastwords']["values"][0]
            #    output["depressive"]['pastwords']["values"].append(v)
            
            
            
            #print(analysis[""], analysis[""],":NOFEATS:", analysis[""], analysis[""])
         
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
            
            #if video['depressed'] == 1:
            #   output["depressive"] = process
            #else:
            #   output["nondepressive"] = process
          
        
        print("---*************Detail Analysis loaded and post processed*****************************----")    
        print(output["depressive"]['wordsentiment']['positive']);
        
       
        #print(output["nondepressive"]['pastwords']["values"]);
        
        if hasnonDepressed:
            output["nondepressive"]['pastwords']["inerquartile"] = InterquartileRange(output["nondepressive"]['pastwords']["values"])
            output["nondepressive"]['pastwords']["std"] = statistics.stdev(output["nondepressive"]['pastwords']["values"])
            output["nondepressive"]['pastwords']["median"] = statistics.median(output["nondepressive"]['pastwords']["values"])
            output["nondepressive"]['pastwords']["mean"] = statistics.mean(output["nondepressive"]['pastwords']["values"])
            output["nondepressive"]['absoultistwords']["inerquartile"] = InterquartileRange(output["nondepressive"]['absoultistwords']["values"])
            output["nondepressive"]['absoultistwords']["std"] = statistics.stdev(output["nondepressive"]['absoultistwords']["values"])
            output["nondepressive"]['absoultistwords']["median"] = statistics.median(output["nondepressive"]['absoultistwords']["values"])
            output["nondepressive"]['absoultistwords']["mean"] = statistics.mean(output["nondepressive"]['absoultistwords']["values"])
            output["nondepressive"]['depressionwords']["inerquartile"] = InterquartileRange(output["nondepressive"]['depressionwords']["values"])
            output["nondepressive"]['depressionwords']["std"] = statistics.stdev(output["nondepressive"]['depressionwords']["values"])
            output["nondepressive"]['depressionwords']["median"] = statistics.median(output["nondepressive"]['depressionwords']["values"])
            output["nondepressive"]['depressionwords']["mean"] = statistics.mean(output["nondepressive"]['depressionwords']["values"])
            output["nondepressive"]['pronoun_fps']["inerquartile"] = InterquartileRange(output["nondepressive"]['pronoun_fps']["values"])
            output["nondepressive"]['pronoun_fps']["std"] = statistics.stdev(output["nondepressive"]['pronoun_fps']["values"])
            output["nondepressive"]['pronoun_fps']["median"] = statistics.median(output["nondepressive"]['pronoun_fps']["values"])
            output["nondepressive"]['pronoun_fps']["mean"] = statistics.mean(output["nondepressive"]['pronoun_fps']["values"])
            output["nondepressive"]['lexdensity']["inerquartile"] = InterquartileRange(output["nondepressive"]['lexdensity']["values"])
            output["nondepressive"]['lexdensity']["std"] = statistics.stdev(output["nondepressive"]['lexdensity']["values"])
            output["nondepressive"]['lexdensity']["median"] = statistics.median(output["nondepressive"]['lexdensity']["values"])
            output["nondepressive"]['lexdensity']["mean"] = statistics.mean(output["nondepressive"]['lexdensity']["values"])
            output["nondepressive"]['clausesperstatement']["inerquartile"] = InterquartileRange(output["nondepressive"]['clausesperstatement']["values"])
            output["nondepressive"]['clausesperstatement']["std"] = statistics.stdev(output["nondepressive"]['clausesperstatement']["values"])
            output["nondepressive"]['clausesperstatement']["median"] = statistics.median(output["nondepressive"]['clausesperstatement']["values"])
            output["nondepressive"]['clausesperstatement']["mean"] = statistics.mean(output["nondepressive"]['clausesperstatement']["values"])
            output["nondepressive"]['sentecelength']["inerquartile"] = InterquartileRange(output["nondepressive"]['sentecelength']["values"])
            output["nondepressive"]['sentecelength']["std"] = statistics.stdev(output["nondepressive"]['sentecelength']["values"])
            output["nondepressive"]['sentecelength']["median"] = statistics.median(output["nondepressive"]['sentecelength']["values"])
            output["nondepressive"]['sentecelength']["mean"] = statistics.mean(output["nondepressive"]['sentecelength']["values"])
            output["nondepressive"]['senteceuniquew']["inerquartile"] = InterquartileRange(output["nondepressive"]['senteceuniquew']["values"])
            output["nondepressive"]['senteceuniquew']["std"] = statistics.stdev(output["nondepressive"]['senteceuniquew']["values"])
            output["nondepressive"]['senteceuniquew']["median"] = statistics.median(output["nondepressive"]['senteceuniquew']["values"])
            output["nondepressive"]['senteceuniquew']["mean"] = statistics.mean(output["nondepressive"]['senteceuniquew']["values"])
            output["nondepressive"]['lexialspohistication']["inerquartile"] = InterquartileRange(output["nondepressive"]['lexialspohistication']["rawlength"])
            output["nondepressive"]['lexialspohistication']["std"] = statistics.stdev(output["nondepressive"]['lexialspohistication']["rawlength"])
            output["nondepressive"]['lexialspohistication']["median"] = statistics.median(output["nondepressive"]['lexialspohistication']["rawlength"])
            output["nondepressive"]['lexialspohistication']["mean"] = statistics.mean(output["nondepressive"]['lexialspohistication']["rawlength"])
            output["nondepressive"]['lexialspohistication']["equationst"] = statistics.stdev(output["nondepressive"]['lexialspohistication']["equationraw"])
            output["nondepressive"]['lexialspohistication']["equationm"] = statistics.median(output["nondepressive"]['lexialspohistication']["equationraw"])
           
            output["nondepressive"]["overallws"] = {"positive": {"mean":statistics.mean( output["nondepressive"]['wordsentiment']['positive']), "median":statistics.median( output["nondepressive"]['wordsentiment']['positive']), "std":statistics.stdev( output["nondepressive"]['wordsentiment']['positive']), "inerquartile":InterquartileRange(output["nondepressive"]['wordsentiment']['positive'])},
                                             "negative": {"mean":statistics.mean( output["nondepressive"]['wordsentiment']['negative']), "median":statistics.median( output["nondepressive"]['wordsentiment']['negative']), "std":statistics.stdev( output["nondepressive"]['wordsentiment']['negative']), "inerquartile":InterquartileRange(output["nondepressive"]['wordsentiment']['negative'])},
                                             "neutral" : {"mean":statistics.mean( output["nondepressive"]['wordsentiment']['neutral']), "median":statistics.median( output["nondepressive"]['wordsentiment']['neutral']), "std":statistics.stdev( output["nondepressive"]['wordsentiment']['neutral']), "inerquartile":InterquartileRange(output["nondepressive"]['wordsentiment']['neutral'])}}
       
            output["nondepressive"]["overallsens"] = {"positive": {"mean":statistics.mean( output["nondepressive"]['sentecesentiment']['positive']), "median":statistics.median( output["nondepressive"]['sentecesentiment']['positive']), "std":statistics.stdev( output["nondepressive"]['sentecesentiment']['positive']), "inerquartile":InterquartileRange(output["nondepressive"]['sentecesentiment']['positive'])},
                                             "negative": {"mean":statistics.mean( output["nondepressive"]['sentecesentiment']['negative']), "median":statistics.median( output["nondepressive"]['sentecesentiment']['negative']), "std":statistics.stdev( output["nondepressive"]['sentecesentiment']['negative']), "inerquartile":InterquartileRange(output["nondepressive"]['sentecesentiment']['negative'])},
                                             "neutral" : {"mean":statistics.mean( output["nondepressive"]['sentecesentiment']['neutral']), "median":statistics.median( output["nondepressive"]['sentecesentiment']['neutral']), "std":statistics.stdev( output["nondepressive"]['sentecesentiment']['neutral']), "inerquartile":InterquartileRange(output["nondepressive"]['sentecesentiment']['neutral'])}}
            
            output["nondepressive"]["lexdensity_tokens"]["avg"] = statistics.mean(  output["nondepressive"]["lexdensity_tokens"]["values"])
            output["nondepressive"]["lexdensity_tokens"]["std"] = statistics.stdev(output["nondepressive"]['lexdensity_tokens']["values"])
            
            
            output["nondepressive"]["lexdensity_senetce"]["avg"] = statistics.mean(  output["nondepressive"]["lexdensity_senetce"]["values"])
            output["nondepressive"]["lexdensity_senetce"]["std"] = statistics.stdev(output["nondepressive"]['lexdensity_senetce']["values"])
            output["nondepressive"]["lexdensity_video"]["avg"] = statistics.mean(  output["nondepressive"]["lexdensity_video"]["values"])
            output["nondepressive"]["lexdensity_video"]["std"] = statistics.stdev(output["nondepressive"]['lexdensity_video']["values"])
            output["nondepressive"]["lexdensity_senetce_nofeats"]["avg"] = statistics.mean(  output["nondepressive"]["lexdensity_senetce_nofeats"]["values"])
            output["nondepressive"]["lexdensity_senetce_nofeats"]["std"] = statistics.stdev(output["nondepressive"]['lexdensity_senetce_nofeats']["values"])
            output["nondepressive"]["lexdensity_video_nofeats"]["avg"] = statistics.mean(  output["nondepressive"]["lexdensity_video_nofeats"]["values"])
            output["nondepressive"]["lexdensity_video_nofeats"]["std"] = statistics.stdev(output["nondepressive"]['lexdensity_video_nofeats']["values"])
            
            output["nondepressive"]["avg_lexdensity_video_nofeats_normalized"]["avg"] = statistics.mean(  output["nondepressive"]["avg_lexdensity_video_nofeats_normalized"]["values"])
            output["nondepressive"]["avg_lexdensity_video_nofeats_normalized"]["std"] = statistics.stdev(output["nondepressive"]['avg_lexdensity_video_nofeats_normalized']["values"])
            
          
       
        if hasDepressed:    
            print("###############################")
            #print(output["depressive"]['pastwords']["values"])    
            #if len(output["depressive"]['pastwords']["values"]) == 1:
            #    v =  output["depressive"]['pastwords']["values"][0]
            #    output["depressive"]['pastwords']["values"].append(v)
            """
            if len( output["depressive"]['pastwords']["values"]) == 1:
                v =  output["depressive"]['pastwords']["values"][0]
                output["depressive"]['pastwords']["values"].append(v)
                
            if len( output["depressive"]['absoultistwords']["values"]) == 1:
                v =  output["depressive"]['absoultistwords']["values"][0]
                output["depressive"]['absoultistwords']["values"].append(v)
            
            if len( output["depressive"]['depressionwords']["values"]) == 1:
                v =  output["depressive"]['depressionwords']["values"][0]
                output["depressive"]['depressionwords']["values"].append(v)
                
            if len( output["depressive"]['pronoun_fps']["values"]) == 1:
                v =  output["depressive"]['pronoun_fps']["values"][0]
                output["depressive"]['pronoun_fps']["values"].append(v)
           
            if len( output["depressive"]['lexdensity']["values"]) == 1:
                v =  output["depressive"]['lexdensity']["values"][0]
                output["depressive"]['lexdensity']["values"].append(v)    
                
            if len( output["depressive"]['clausesperstatement']["values"]) == 1:
                v =  output["depressive"]['clausesperstatement']["values"][0]
                output["depressive"]['clausesperstatement']["values"].append(v) 
                
            if len( output["depressive"]['sentecelength']["values"]) == 1:
                v =  output["depressive"]['sentecelength']["values"][0]
                output["depressive"]['sentecelength']["values"].append(v) 
                
            if len( output["depressive"]['senteceuniquew']["values"]) == 1:
                v =  output["depressive"]['senteceuniquew']["values"][0]
                output["depressive"]['senteceuniquew']["values"].append(v) 
                
            if len( output["depressive"]['lexialspohistication']["equationraw"]) == 1:
                v =  output["depressive"]['lexialspohistication']["equationraw"][0]
                output["depressive"]['lexialspohistication']["equationraw"].append(v) 
                
                
            """
            #if len( output["depressive"]['lexialspohistication']["values"]) == 1:
                #v =  output["depressive"]['lexialspohistication']["values"][0]
                #output["depressive"]['lexialspohistication']["values"].append(v) 
             
            output["depressive"]['pastwords']["inerquartile"] = InterquartileRange(output["depressive"]['pastwords']["values"])          
            output["depressive"]['pastwords']["std"] = statistics.stdev(output["depressive"]['pastwords']["values"])          
            output["depressive"]['pastwords']["median"] = statistics.median(output["depressive"]['pastwords']["values"])         
            output["depressive"]['pastwords']["mean"] = statistics.mean(output["depressive"]['pastwords']["values"])            
            output["depressive"]['absoultistwords']["inerquartile"] = InterquartileRange(output["depressive"]['absoultistwords']["values"])            
            output["depressive"]['absoultistwords']["std"] = statistics.stdev(output["depressive"]['absoultistwords']["values"])          
            output["depressive"]['absoultistwords']["median"] = statistics.median(output["depressive"]['absoultistwords']["values"])          
            output["depressive"]['absoultistwords']["mean"] = statistics.mean(output["depressive"]['absoultistwords']["values"])                    
            output["depressive"]['depressionwords']["inerquartile"] = InterquartileRange(output["depressive"]['depressionwords']["values"])          
            output["depressive"]['depressionwords']["std"] = statistics.stdev(output["depressive"]['depressionwords']["values"])         
            output["depressive"]['depressionwords']["median"] = statistics.median(output["depressive"]['depressionwords']["values"])           
            output["depressive"]['depressionwords']["mean"] = statistics.mean(output["depressive"]['depressionwords']["values"])                               
            output["depressive"]['pronoun_fps']["inerquartile"] = InterquartileRange(output["depressive"]['pronoun_fps']["values"])           
            output["depressive"]['pronoun_fps']["std"] = statistics.stdev(output["depressive"]['pronoun_fps']["values"])         
            output["depressive"]['pronoun_fps']["median"] = statistics.median(output["depressive"]['pronoun_fps']["values"])           
            output["depressive"]['pronoun_fps']["mean"] = statistics.mean(output["depressive"]['pronoun_fps']["values"])                                  
            output["depressive"]['lexdensity']["inerquartile"] = InterquartileRange(output["depressive"]['lexdensity']["values"])        
            output["depressive"]['lexdensity']["std"] = statistics.stdev(output["depressive"]['lexdensity']["values"])          
            output["depressive"]['lexdensity']["median"] = statistics.median(output["depressive"]['lexdensity']["values"])          
            output["depressive"]['lexdensity']["mean"] = statistics.mean(output["depressive"]['lexdensity']["values"])
            output["depressive"]['clausesperstatement']["inerquartile"] = InterquartileRange(output["depressive"]['clausesperstatement']["values"])            
            output["depressive"]['clausesperstatement']["std"] = statistics.stdev(output["depressive"]['clausesperstatement']["values"])          
            output["depressive"]['clausesperstatement']["median"] = statistics.median(output["depressive"]['clausesperstatement']["values"])        
            output["depressive"]['clausesperstatement']["mean"] = statistics.mean(output["depressive"]['clausesperstatement']["values"])
            output["depressive"]['sentecelength']["inerquartile"] = InterquartileRange(output["depressive"]['sentecelength']["values"])
            output["depressive"]['sentecelength']["std"] = statistics.stdev(output["depressive"]['sentecelength']["values"])  
            output["depressive"]['sentecelength']["median"] = statistics.median(output["depressive"]['sentecelength']["values"])      
            output["depressive"]['sentecelength']["mean"] = statistics.mean(output["depressive"]['sentecelength']["values"])
            output["depressive"]['senteceuniquew']["inerquartile"] = InterquartileRange(output["depressive"]['senteceuniquew']["values"])  
            output["depressive"]['senteceuniquew']["std"] = statistics.stdev(output["depressive"]['senteceuniquew']["values"])        
            output["depressive"]['senteceuniquew']["median"] = statistics.median(output["depressive"]['senteceuniquew']["values"])         
            output["depressive"]['senteceuniquew']["mean"] = statistics.mean(output["depressive"]['senteceuniquew']["values"])
            output["depressive"]['lexialspohistication']["inerquartile"] = InterquartileRange(output["depressive"]['lexialspohistication']["rawlength"])          
            output["depressive"]['lexialspohistication']["std"] = statistics.stdev(output["depressive"]['lexialspohistication']["rawlength"])        
            output["depressive"]['lexialspohistication']["median"] = statistics.median(output["depressive"]['lexialspohistication']["rawlength"])          
            output["depressive"]['lexialspohistication']["mean"] = statistics.mean(output["depressive"]['lexialspohistication']["rawlength"])  
            output["depressive"]['lexialspohistication']["equationst"] = statistics.stdev(output["depressive"]['lexialspohistication']["equationraw"])         
            output["depressive"]['lexialspohistication']["equationm"] = statistics.median(output["depressive"]['lexialspohistication']["equationraw"])
            
            output["depressive"]["overallws"] = {"positive": {"mean":statistics.mean( output["depressive"]['wordsentiment']['positive']), "median":statistics.median( output["depressive"]['wordsentiment']['positive']), "std":statistics.stdev( output["depressive"]['wordsentiment']['positive']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['positive'])},
                                                 "negative": {"mean":statistics.mean( output["depressive"]['wordsentiment']['negative']), "median":statistics.median( output["depressive"]['wordsentiment']['negative']), "std":statistics.stdev( output["depressive"]['wordsentiment']['negative']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['negative'])},
                                                 "neutral" : {"mean":statistics.mean( output["depressive"]['wordsentiment']['neutral']), "median":statistics.median( output["depressive"]['wordsentiment']['neutral']), "std":statistics.stdev( output["depressive"]['wordsentiment']['neutral']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['neutral'])}} 
            output["depressive"]["overallsens"] = {"positive": {"mean":statistics.mean( output["depressive"]['sentecesentiment']['positive']), "median":statistics.median( output["depressive"]['wordsentiment']['positive']), "std":statistics.stdev( output["depressive"]['wordsentiment']['positive']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['positive'])},
                                                 "negative": {"mean":statistics.mean( output["depressive"]['sentecesentiment']['negative']), "median":statistics.median( output["depressive"]['wordsentiment']['negative']), "std":statistics.stdev( output["depressive"]['wordsentiment']['negative']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['negative'])},
                                                 "neutral" : {"mean":statistics.mean( output["depressive"]['sentecesentiment']['neutral']), "median":statistics.median( output["depressive"]['wordsentiment']['neutral']), "std":statistics.stdev( output["depressive"]['wordsentiment']['neutral']), "inerquartile":InterquartileRange(output["depressive"]['wordsentiment']['neutral'])}}
            
            output["depressive"]["lexdensity_tokens"]["avg"] = statistics.mean(output["depressive"]["lexdensity_tokens"]["values"])
            output["depressive"]["lexdensity_tokens"]["std"] = statistics.stdev(output["depressive"]['lexdensity_tokens']["values"])
            
            output["depressive"]["lexdensity_senetce"]["avg"] = statistics.mean(  output["depressive"]["lexdensity_senetce"]["values"])
            output["depressive"]["lexdensity_senetce"]["std"] = statistics.stdev(output["depressive"]['lexdensity_senetce']["values"])
            output["depressive"]["lexdensity_video"]["avg"] = statistics.mean(  output["depressive"]["lexdensity_video"]["values"])
            output["depressive"]["lexdensity_video"]["std"] = statistics.stdev(output["depressive"]['lexdensity_video']["values"])
            output["depressive"]["lexdensity_senetce_nofeats"]["avg"] = statistics.mean(  output["depressive"]["lexdensity_senetce_nofeats"]["values"])
            output["depressive"]["lexdensity_senetce_nofeats"]["std"] = statistics.stdev(output["depressive"]['lexdensity_senetce_nofeats']["values"])
            output["depressive"]["lexdensity_video_nofeats"]["avg"] = statistics.mean(  output["depressive"]["lexdensity_video_nofeats"]["values"])
            output["depressive"]["lexdensity_video_nofeats"]["std"] = statistics.stdev(output["depressive"]['lexdensity_video_nofeats']["values"])
            
            output["depressive"]["avg_lexdensity_video_nofeats_normalized"]["avg"] = statistics.mean(  output["depressive"]["avg_lexdensity_video_nofeats_normalized"]["values"])
            output["depressive"]["avg_lexdensity_video_nofeats_normalized"]["std"] = statistics.stdev(output["depressive"]['avg_lexdensity_video_nofeats_normalized']["values"])
                
        return output, hasDepressed, hasnonDepressed 
    
    def PrepareforCSV(self,analysissource):
        f = open(analysissource)
        data = json.load(f)
        f.close();
            
        hasDepressed = False;
        hasnonDepressed = False;
        
        process = {            
            "video_id": [],                       
            #"A1_first_p_pronouns_percentage": [],
            "A1_Use_of_firstperson_pronouns_percentage": [],
            #"A1_first_p_pronouns_words_normalized": [],
            "A1_Use_of_firstperson_pronouns_words_normalized": [],            
            "A2_Use_of_negatively_valanced_words":{"positive":{"percentage":[], "words_normalized" : []}, "negative":{"percentage":[], "words_normalized" : []}, "neutral":{"percentage":[], "words_normalized" : []} },
            "A2_sentiment_sentece":{"positive":{"percentage":[], "sentece_normalized" : []}, "negative":{"percentage":[], "sentece_normalized" : []}, "neutral":{"percentage":[], "sentece_normalized" : []} },
            "A2_Use_of_negatively_valanced_words_positive_percentage":[],
            "A2_Use_of_negatively_valanced_words_positive_words_normalized":[],
            "A2_Use_of_negatively_valanced_words_negative_percentage":[],
            "A2_Use_of_negatively_valanced_words_negative_words_normalized":[],
            "A2_Use_of_negatively_valanced_words_neutral_percentage":[],
            "A2_Use_of_negatively_valanced_words_neutral_words_normalized":[],
            "A2_Sentiment_sentece_positive_percentage":[],
            "A2_Sentiment_sentece_negative_percentage":[],
            "A2_Sentiment_sentece_neutral_percentage":[],
            "A3_Explicit_mention_of_depression_words_percentage": [],
            "A3_Explicit_mention_of_depression_words_normalized":[],
            "A4_Use_of_absolutist_words_small_percentage": [],
            "A4_Use_of_absolutist_words_small_normalized":[],
            "A4_Use_of_absolutist_words_full_percentage": [],
            "A4_Use_of_absolutist_words_full_normalized":[],
            "A5_Focusing_on_past_words_percentage": [],
            "A5_Focusing_on_past_words_normalized":[],
            "A61_1_Syntactic_complexity_sentece_length": [],
            "A61_2_Syntactic_complexity_sentece_complexity": [],
            #"A621_Lexical_diversity_words": [],
            #"A621_Lexical_diversity_words_percentage": [],
            #"A621_Lexical_diversity_words_normalized": [],
            #"A62_1_Lexical_complexity_lexical_diversity_persentece": [],
            "A62_1_Lexical_complexity_lexical_diversity": [],
            "A62_2_Lexical_complexity_lexical_sophistication": [],
            "A62_3_Lexical_complexity_lexical_density": [],
            #"avg_lexdensity_per_specific_POS": [],
            #"avg_lexdensity_senetece": [],
            #"avg_lexdensity_senetece_nofeats": [],
            #"avg_lexdensity_video" : [],
            #"avg_lexdensity_video_nofeats" : [],
            #"avg_lexdensity_video_normalized" : [],
            #"avg_lexdensity_video_nofeats_normalized" : [],
            "total_videos": 0,
            "depressive": []            
        }
     
        
        for id in data:
            videodetails = data [str(id)]
            #process["total_videos"]+=1;
            if videodetails['depressed'] == 1:
                #process = analysis["depressive"] 
                process["depressive"].append(1)
                hasDepressed = True
            else:
                #process = analysis["nondepressive"]
                process["depressive"].append(0) 
                hasnonDepressed = True
            
            process["video_id"].append(str(id))
            #1. A1_use_of_first_p_pronouns
            
          
            
            #process["avg_lexdensity_senetece"].append(videodetails["avg_lexdensity_senetece"])
            #process["avg_lexdensity_senetece_nofeats"].append(videodetails["avg_lexdensity_senetece_nofeats"])
            #process["avg_lexdensity_per_specific_POS"].append(videodetails["avg_lexdensity_per_specific_POS"])
            #process["avg_lexdensity_video"].append(videodetails["avg_lexdensity_video"])
            #process["avg_lexdensity_video_nofeats"].append(videodetails["avg_lexdensity_video_nofeats"])
            #process["avg_lexdensity_video_normalized"].append(videodetails["avg_lexdensity_video_normalized"])
            #process["avg_lexdensity_video_nofeats_normalized"].append(videodetails["avg_lexdensity_video_nofeats_normalized"])

                        
            process["A1_Use_of_firstperson_pronouns_percentage"].append(videodetails["language"]["pronoun_fps"]["percentage"])
            process["A1_Use_of_firstperson_pronouns_words_normalized"].append(  
                round((videodetails["language"]["pronoun_fps"]["count"]/videodetails["words"])*1000,2)
                ) 
            
            #2.1 A2_use_of_first_p_pronouns: words
            process["A2_Use_of_negatively_valanced_words"]["positive"]["percentage"].append(videodetails["language"]["sentiment"]["word"]["positive"]["percentage"])
            process["A2_Use_of_negatively_valanced_words_positive_percentage"].append(videodetails["language"]["sentiment"]["word"]["positive"]["percentage"])
            
            process["A2_Use_of_negatively_valanced_words"]["positive"]["words_normalized"].append(  
                round((videodetails["language"]["sentiment"]["word"]["positive"]["count"]/videodetails["words"])*1000,2)
                ) 
            process["A2_Use_of_negatively_valanced_words_positive_words_normalized"].append(  
                round((videodetails["language"]["sentiment"]["word"]["positive"]["count"]/videodetails["words"])*1000,2)
                ) 
            
            process["A2_Use_of_negatively_valanced_words"]["negative"]["percentage"].append(videodetails["language"]["sentiment"]["word"]["negative"]["percentage"])
            process["A2_Use_of_negatively_valanced_words_negative_percentage"].append(videodetails["language"]["sentiment"]["word"]["negative"]["percentage"])
            process["A2_Use_of_negatively_valanced_words"]["negative"]["words_normalized"].append(  
                round((videodetails["language"]["sentiment"]["word"]["negative"]["count"]/videodetails["words"])*1000,2)
                ) 
            process["A2_Use_of_negatively_valanced_words_negative_words_normalized"].append(  
                round((videodetails["language"]["sentiment"]["word"]["negative"]["count"]/videodetails["words"])*1000,2)
                ) 
            
            
            process["A2_Use_of_negatively_valanced_words"]["neutral"]["percentage"].append(videodetails["language"]["sentiment"]["word"]["neutral"]["percentage"])
            process["A2_Use_of_negatively_valanced_words_neutral_percentage"].append(videodetails["language"]["sentiment"]["word"]["neutral"]["percentage"])
            process["A2_Use_of_negatively_valanced_words"]["neutral"]["words_normalized"].append(  
                round((videodetails["language"]["sentiment"]["word"]["neutral"]["count"]/videodetails["words"])*1000,2)
                )
            process["A2_Use_of_negatively_valanced_words_neutral_words_normalized"].append(  
                round((videodetails["language"]["sentiment"]["word"]["neutral"]["count"]/videodetails["words"])*1000,2)
                )
            
            
            
            #2.2 A2_use_of_first_p_pronouns: senetce
            process["A2_sentiment_sentece"]["positive"]["percentage"].append(videodetails["language"]["sentiment"]["sentence"]["positive"]["percentage"])
            process["A2_Sentiment_sentece_positive_percentage"].append(videodetails["language"]["sentiment"]["sentence"]["positive"]["percentage"])
            process["A2_sentiment_sentece"]["positive"]["sentece_normalized"].append(  
                round((videodetails["language"]["sentiment"]["sentence"]["positive"]["count"]/videodetails["sentences"])*1000,2)
                ) 
            process["A2_sentiment_sentece"]["negative"]["percentage"].append(videodetails["language"]["sentiment"]["sentence"]["negative"]["percentage"])
            process["A2_Sentiment_sentece_negative_percentage"].append(videodetails["language"]["sentiment"]["sentence"]["negative"]["percentage"])
            process["A2_sentiment_sentece"]["negative"]["sentece_normalized"].append(  
                round((videodetails["language"]["sentiment"]["word"]["negative"]["count"]/videodetails["sentences"])*1000,2)
                ) 
            process["A2_sentiment_sentece"]["neutral"]["percentage"].append(videodetails["language"]["sentiment"]["sentence"]["neutral"]["percentage"])
            process["A2_Sentiment_sentece_neutral_percentage"].append(videodetails["language"]["sentiment"]["sentence"]["neutral"]["percentage"])
            process["A2_sentiment_sentece"]["neutral"]["sentece_normalized"].append(  
                round((videodetails["language"]["sentiment"]["word"]["neutral"]["count"]/videodetails["sentences"])*1000,2)
                )
            #3. A3_explicit_deression
            process["A3_Explicit_mention_of_depression_words_percentage"].append(videodetails["language"]["depression_specific_words"]["percentage"])
            process["A3_Explicit_mention_of_depression_words_normalized"].append(  
                round((videodetails["language"]["depression_specific_words"]["count"]/videodetails["words"])*1000,2)
                ) 
            #4.1 A4_absolutist_words_small
            process["A4_Use_of_absolutist_words_small_percentage"].append(videodetails["language"]["absolutist_words_small"]["percentage"])
            process["A4_Use_of_absolutist_words_small_normalized"].append(  
                round((videodetails["language"]["absolutist_words_small"]["count"]/videodetails["words"])*1000,2)
                ) 
            #4.1 A4_absolutist_words_full
            process["A4_Use_of_absolutist_words_full_percentage"].append(videodetails["language"]["absolutist_words_full"]["percentage"])
            process["A4_Use_of_absolutist_words_full_normalized"].append(  
                round((videodetails["language"]["absolutist_words_full"]["count"]/videodetails["words"])*1000,2)
                )
            #5 A5_pastwords
            process["A5_Focusing_on_past_words_percentage"].append(videodetails["language"]["pastwords"]["percentage"])
            process["A5_Focusing_on_past_words_normalized"].append(  
                round((videodetails["language"]["pastwords"]["count"]/videodetails["words"])*1000,2)
                )
            #6.1.1: A611_sentece_length 
            process["A61_1_Syntactic_complexity_sentece_length"].append(videodetails["sentece_length_data"]["avg"])
        
            #6.1.2: A612_sentece_complexity 
            process["A61_2_Syntactic_complexity_sentece_complexity"].append(videodetails["clauses_data"]["avg"])
            
            #6.2.1: A621_Lexical_diversity: unqiue words         
           # process["A621_Lexical_diversity_words"].append(videodetails["unique_words"]["count"])
           # process["A621_Lexical_diversity_words_percentage"].append(videodetails["unique_words"]["percentage"])
           # process["A621_Lexical_diversity_words_normalized"].append(  
           #     round((videodetails["unique_words"]["count"]/videodetails["words"])*1000,2)
           #     )
            process["A62_1_Lexical_complexity_lexical_diversity"].append(videodetails["sentece_unique_words_data"]["avg"])
            
            #6.2.2: A622_Lexical_spohistication
            process["A62_2_Lexical_complexity_lexical_sophistication"].append(videodetails["lexsophistication_data"]["avg"])
            
            #6.2.3: A623_Lexical_density
            process["A62_3_Lexical_complexity_lexical_density"].append(round(sum(list(chain.from_iterable(videodetails["language"]["lexialspohistication_per_statement"]["map"])))*100/videodetails['words'],4)) 
            
        return process #analysis;



def post_process_sentiment(data, analysis):
    statements = 0;
    words = 0; 
    senteces = 0;
   # print (data)
    for d in data:
         analysis["words"]+=d["word"]["negative"] + d["word"]["positive"] + d["word"]["neutral"]
         analysis["sentences"]+=d["sentence"]["negative"] + d["sentence"]["positive"] + d["sentence"]["neutral"]
         analysis["statements"]+=d["statement"]["negative"] + d["statement"]["positive"] + d["statement"]["neutral"]
        
         analysis["language"]["sentiment"]["statement"]["positive"]["count"] +=  d["statement"]["positive"]
         analysis["language"]["sentiment"]["statement"]["negative"]["count"] +=  d["statement"]["negative"]
         analysis["language"]["sentiment"]["statement"]["neutral"]["count"] +=  d["statement"]["neutral"]
         
         analysis["language"]["sentiment"]["sentence"]["positive"]["count"] +=  d["sentence"]["positive"]
         analysis["language"]["sentiment"]["sentence"]["negative"]["count"] +=  d["sentence"]["negative"]
         analysis["language"]["sentiment"]["sentence"]["neutral"]["count"] +=  d["sentence"]["neutral"]
         
         analysis["language"]["sentiment"]["word"]["positive"]["count"] +=d["word"]["positive"]
         analysis["language"]["sentiment"]["word"]["negative"]["count"] +=d["word"]["negative"]
         analysis["language"]["sentiment"]["word"]["neutral"]["count"] +=d["word"]["neutral"]
         
    
    analysis["language"]["sentiment"]["statement"]["positive"]["percentage"] = round(analysis["language"]["sentiment"]["statement"]["positive"]["count"]/analysis["statements"], 4)
    analysis["language"]["sentiment"]["statement"]["negative"]["percentage"] = round(analysis["language"]["sentiment"]["statement"]["negative"]["count"]/analysis["statements"], 4)
    analysis["language"]["sentiment"]["statement"]["neutral"]["percentage"] = round(analysis["language"]["sentiment"]["statement"]["neutral"]["count"]/analysis["statements"], 4)
    
    analysis["language"]["sentiment"]["sentence"]["positive"]["percentage"] = round(analysis["language"]["sentiment"]["sentence"]["positive"]["count"]/analysis["sentences"], 4)
    analysis["language"]["sentiment"]["sentence"]["negative"]["percentage"] = round(analysis["language"]["sentiment"]["sentence"]["negative"]["count"]/analysis["sentences"], 4)
    analysis["language"]["sentiment"]["sentence"]["neutral"]["percentage"] = round(analysis["language"]["sentiment"]["sentence"]["neutral"]["count"]/analysis["sentences"], 4)
    
    analysis["language"]["sentiment"]["word"]["positive"]["percentage"] = round(analysis["language"]["sentiment"]["word"]["positive"]["count"]/analysis["words"], 4)
    analysis["language"]["sentiment"]["word"]["negative"]["percentage"] = round(analysis["language"]["sentiment"]["word"]["negative"]["count"]/analysis["words"], 4)
    analysis["language"]["sentiment"]["word"]["neutral"]["percentage"] = round(analysis["language"]["sentiment"]["word"]["neutral"]["count"]/analysis["words"], 4)



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


def process_specific_words(line, analysis):
    print(line);
    regex_aw_f = '|'.join(ABSOULTIST_GRAMMAR_F).lower()
    regex_aw_s =  '|'.join(ABSOULTIST_GRAMMAR_S).lower()
    regex_dw =  '|'.join(DEPRESSIVE_GRAMMAR).lower()
    
    x = len(re.findall(r'\b(%s)\b' % regex_aw_f, line.lower()))
    y = len(re.findall(r'\b(%s)\b' % regex_aw_s, line.lower()))
    z = len(re.findall(r'\b(%s)\b' % regex_dw, line.lower()))
    
    analysis["language"]["absolutist_words_full"]["count"]+=x
    analysis["language"]["absolutist_words_small"]["count"]+=y
    analysis["language"]["depression_specific_words"]["count"]+=z
    
   # analysis["language"]["pronoun_fps"]["percentage"] = round(analysis["language"]["pronoun_fps"]["count"]/analysis["words"],4)
    #print(x,",", y,"|", analysis["language"]["absolutist_words_full"]["count"],",", analysis["language"]["absolutist_words_small"]["count"])    
def post_process_firstpersonpronouns(data, analysis):       
    print(data)
    for d in data:
         analysis["language"]["pronoun_fps"]["count"]+= d
    
    analysis["language"]["pronoun_fps"]["percentage"] = round(analysis["language"]["pronoun_fps"]["count"]/analysis["words"],4)

def post_process_pastwords(data, analysis):       
    print(data)
    for d in data:
         analysis["language"]["pastwords"]["count"]+= d
    
    analysis["language"]["pastwords"]["percentage"] = round(analysis["language"]["pastwords"]["count"]/analysis["words"],4)

    

