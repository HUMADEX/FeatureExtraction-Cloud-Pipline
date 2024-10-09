'''
Created on 18. avg. 2022

@author: izido
'''

import stanza
import re

import nltk
from nltk.corpus import wordnet as wn

import spacy
import numpy as np





class StaznaLanguageExtractionPipline:
   
    def __init__(self, language = 'en'):
       print("START")
       self.nlp = stanza.Pipeline(lang=language, processors='tokenize,sentiment,mwt,pos')
       self.nlp_nosplit = stanza.Pipeline(lang=language, processors='tokenize,sentiment,mwt,pos', tokenize_no_ssplit=True)
       self.spacy_nlp = spacy.load("en_core_web_sm")
       
       # nouns, verbs, adjectives, and adverbs
       
       self.LEXIAL_SOPHISTICATION_UPOS = ["ADJ", "ADV", "NOUN", "VERB", "PROPN"] 
       
       
       

       
       
    def LoadData(self, inputtext):  
       self.senetces = [];
       self.words = [];

       self.inputtext = inputtext;
       #print("inputtext:",inputtext)
      
       self.doc = self.nlp(self.inputtext)
       self.doc_nosplit = self.nlp_nosplit(self.inputtext)
       self.senetces = [sent.text for sent in self.doc.sentences]
       
       words_per_senetce = [];  
       distinct_words_per_sentece = [];     
       word_lengths = [];
       for i, sentence in enumerate(self.doc.sentences):
          distinct_words = [];
         
          word_count = 0;
          for token in sentence.words:
            #print(token)
            if not token.upos == "PUNCT":
               #print(token)
               self.words.append(token.text) 
               distinct_words.append(token.text)
               word_count+=1
               word_lengths.append(len(token.text)) 
            
          #print(sentence.text, word_count)    
          unique_words = self.unique(distinct_words); 
          print(unique_words)
          distinct_words_per_sentece.append(len(unique_words))
          words_per_senetce.append(word_count)  
          
       return words_per_senetce, distinct_words_per_sentece, self.words, word_lengths;
   
    def SentimentAnalysis(self):     
        sentiment = {"statement": {"negative":0, "positive":0, "neutral":0},"sentence": {"negative":0, "positive":0, "neutral":0}, "word": {"negative":0, "positive":0, "neutral":0}}
        
        for i, sentence in enumerate(self.doc_nosplit.sentences):
            if sentence.sentiment == 0:
                sentiment["statement"]["negative"] +=1   
            elif sentence.sentiment == 1: 
                 sentiment["statement"]["neutral"] +=1   
            elif sentence.sentiment == 2: 
                 sentiment["statement"]["positive"] +=1
     
        for i, sentence in enumerate(self.doc.sentences):  
            if sentence.sentiment == 0:
                sentiment["sentence"]["negative"] +=1   
            elif sentence.sentiment == 1: 
                 sentiment["sentence"]["neutral"] +=1   
            elif sentence.sentiment == 2: 
                 sentiment["sentence"]["positive"] +=1     
           # print("%d %s -> %d" % (i, sentence.text, sentence.sentiment))
        #print(self.words)
        for word in self.words:
           
            docword = self.nlp(word)
            for i, w in enumerate(docword.sentences): 
                if w.sentiment == 0:
                    sentiment["word"]["negative"] +=1   
                elif w.sentiment == 1: 
                     sentiment["word"]["neutral"] +=1   
                elif w.sentiment == 2: 
                     sentiment["word"]["positive"] +=1  
             
        return sentiment
    
    def PronounAnalysis(self):
        count = 0;
        for i, sentence in enumerate(self.doc.sentences):
            for j, word in enumerate(sentence.words):
                 if not word.upos == "PUNCT":
                   
                    #print(word.text, '\t', word.upos, '\t', word.xpos,  '\t', word.feats)  
                    if word.feats and re.search("Number=Sing", word.feats) and (word.upos == 'PRON' or word.xpos == 'PRP'):
                        if re.search("Person=1", word.feats) or re.search("Poss=Yes", word.feats) and not re.search("Person=2|Person=3", word.feats):
                            #print(".......First person singular pronouns........")
                            #print(word.text, '\t', word.upos, '\t', word.xpos,  '\t', word.feats)
                            count +=1
                     # and re.search("Person=1", word.feats) or re.search("Poss=Yes", word.feats) and not (re.search("Person=3", word.feats))) :
        return count;
    
    def LexicalSophisticationAndDensity(self):   
        print("******************LexicalSophisticationAndDensity****************************") 
        count = [];
        detail = [];
        lengths =  [];
        disticntstemspersentece = [];
        disticntwordtypespersentece = []; 
        for i, sentence in enumerate(self.doc.sentences):
            per_senetece = 0;
            wps = 0;
            stemtype = {"wps" : 0, "typesmap": [], "value": 0.00, "text":""}
            wordtype = {"wps" : 0, "typesmap": [], "value": 0.00}
            for j, word in enumerate(sentence.words):
                 #print(word.text, '\t', word.upos, '\t', word.xpos)
                 wlength = []
                 if not word.upos == "PUNCT":
                    wps+=1;
                    token = word.upos + "|" + (word.feats if word.feats else "")
                   # print("Token#########################",token)
                    stemtype["text"] += word.text + "*"
                    #print(word.text, '\t', word.upos, '\t', word.xpos,  '\t', word.feats)  
                    stemtype["typesmap"].append(token)
                    
                    wordtype["typesmap"].append(word.upos)
                    if word.feats  and (word.upos in self.LEXIAL_SOPHISTICATION_UPOS):
                        #if re.search("Person=1", word.feats) or re.search("Poss=Yes", word.feats) and not re.search("Person=2|Person=3", word.feats):
                            #print(".......First person singular pronouns........")
                            print(word.text, '\t', word.upos, '\t', word.xpos,  '\t', word.feats)
                            per_senetece +=1
                            lengths.append(len(word.text))
                           
                     # and re.search("Person=1", word.feats) or re.search("Poss=Yes", word.feats) and not (re.search("Person=3", word.feats))) :
            if wps > 0: 
                stemtype["wps"] = wps;
                wordtype["wps"] = wps;
                #utype["text"] = " ".join(enumerate(sentence.words));
                count.append(per_senetece)
                #Get Unique Values
                list_set = set(stemtype["typesmap"])
                stemtype["typesmap"] = (list(list_set));
                stemtype["value"] = round(len(stemtype["typesmap"])/wps,4)
                disticntstemspersentece.append(stemtype);
                
                list_set_W = set(wordtype["typesmap"])
                wordtype["typesmap"] = (list(list_set_W));
                wordtype["value"] = round(len(wordtype["typesmap"])/wps,4)
                disticntwordtypespersentece.append(wordtype);
                
                
                detail.append({"count" : per_senetece, "percent": round(per_senetece/wps,4) if wps > 0 else 0, "lengths": lengths})
               # print("Token#########################")
               # print(disticntstemspersentece)
                #print("Token#########################")
                #print(disticntwordtypespersentece)
        return count, detail, disticntstemspersentece, disticntwordtypespersentece;          #       print(word.text, '\t', word.upos, '\t', word.xpos,  '\t', word.feats)
                           
    def PastWordsAnalysis(self):
        count = 0;
        for i, sentence in enumerate(self.doc.sentences):
            for j, word in enumerate(sentence.words):
                 if not word.upos == "PUNCT":

                    #print(word.text, '\t', word.upos, '\t', word.xpos,  '\t', word.feats)  
                    if  word.feats and re.search("Tense=Past", word.feats) and (word.upos == 'VERB' or word.xpos in ['VB','VBD','VBG', 'VHN', 'VVD', 'VVN', 'VHD']):
                        #print(word.text, '\t', word.upos, '\t', word.xpos,  '\t', word.feats)
                        #word.feats and re.search("Number=Sing", word.feats) and
                        #if re.search("Person=1", word.feats) or re.search("Poss=Yes", word.feats) and not re.search("Person=2|Person=3", word.feats):
                            #print(".......First person singular pronouns........")
                            #print(word.text, '\t', word.upos, '\t', word.xpos,  '\t', word.feats)
                        count +=1
                     # and re.search("Person=1", word.feats) or re.search("Poss=Yes", word.feats) and not (re.search("Person=3", word.feats))) :
        return count;
    
    
    
    def CaluseRatio(self):  
        SUBJECTS = {"nsubj", "nsubjpass", "csubj", "csubjpass", "agent", "expl"}
        
        OBJECTS = {"dobj", "dative", "attr", "oprd"}
        
        BREAKER_POS = {"CCONJ", "VERB"}
        
        NEGATIONS = {"no", "not", "n't", "never", "none"}
        
        clasues_per_Sentece = [];
             
        for i, sentence in enumerate(self.doc.sentences):
            #print("Clauses for sentece:", sentence.text)
            sdoc = self.spacy_nlp(sentence.text)

            
            root_token = self.find_root_of_sentence(sdoc)
            other_verbs = self.find_other_verbs(sdoc, root_token)
            
            token_spans = []   
            all_verbs = [root_token] + other_verbs
            for other_verb in all_verbs:
                (first_token_index, last_token_index) = \
                 self.get_clause_token_span_for_verb(other_verb, 
                                                sdoc, all_verbs)
                token_spans.append((first_token_index, 
                                    last_token_index))
                
            sentence_clauses = []
            for token_span in token_spans:
                start = token_span[0]
                end = token_span[1]
                if (start < end):
                    clause = sdoc[start:end]
                    sentence_clauses.append(clause)
                    
            sentence_clauses = sorted(sentence_clauses, 
                                      key=lambda tup: tup[0])
            
            clauses_text = [clause.text for clause in sentence_clauses]
            #print("clasues extracted:",clauses_text)
            if len(clauses_text) > 0:
                clasues_per_Sentece.append(len(clauses_text));
            else:
                clasues_per_Sentece.append(1);
        
        return clasues_per_Sentece;   
                 
    def find_root_of_sentence(self, doc):
        root_token = None
        for token in doc:
            if (token.dep_ == "ROOT"):
                root_token = token
        return root_token
    
    def find_other_verbs(self, doc, root_token):
        other_verbs = []
        for token in doc:
            ancestors = list(token.ancestors)
            if (token.pos_ == "VERB" and len(ancestors) == 1\
                and ancestors[0] == root_token):
                other_verbs.append(token)
        return other_verbs
    
    def get_clause_token_span_for_verb(self, verb, doc, all_verbs):
        first_token_index = len(doc)
        last_token_index = 0
        this_verb_children = list(verb.children)
        for child in this_verb_children:
            if (child not in all_verbs):
                if (child.i < first_token_index):
                    first_token_index = child.i
                if (child.i > last_token_index):
                    last_token_index = child.i
        return(first_token_index, last_token_index)
    
    def unique(self,list1):
        x = np.array(list1)
        return np.unique(x).tolist()
           
             
             #for j, word in enumerate(sentence.words):
             #    print()
                         
class NLTKLanguageExtractionPipline: 
    
    
    def __init__(self, language = 'en'):     
        nltk.download()
    
 
                     
           