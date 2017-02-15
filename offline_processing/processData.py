from joblib import Parallel, delayed
import matplotlib.pyplot as plt
import multiprocessing
import pandas as pd
import numpy as np
import glob

INDEX_COLUMN = 't'
LABEL_COLUMN = 'class'
_INPUT_FILE_ = 'arjun-2/all.csv'


wifi_hash = set([])
train_data_dir = glob.glob('training_data/*.csv')
test_data_dir  = glob.glob('test_data/*.csv')
num_cores      = 1#multiprocessing.cpu_count()

labels   = ['one',        'two',     'three',    'arjun_bathroom', 'four',       'arjun_room', 'unknown', 'current_label']
readable = ['piano_room', 'kitchen', 'bathroom', 'bathroom',       'arjun_room', 'arjun_room', '?', '?']
def importFile(file_):
    global wifi_hash
    dframe = pd.read_csv(file_, names=["t", "SSID", "RSSI", "class"])
    [wifi_hash.add(e) for e in dframe['SSID']]
    for l,r in zip(labels, readable): dframe.loc[dframe['class'] == l, 'class'] = r
    dframe.drop(dframe[dframe['class']=='?'].index, inplace=True)
    print 'imported', file_
    return dframe

def buildFeatureList(data):
    size_d = len(data)
    global wifi_hash
    for ssid in list(wifi_hash):data[ssid] = pd.Series(np.empty(size_d) * np.nan, index=data.index)
    for x in xrange(0, size_d):
        data.set_value(x, data.ix[x]['SSID'],  data.ix[x]['RSSI'])
        #data.set_value(x, data.ix[x]['class'], readable[labels.index(data.ix[x]['class'])])
        #data.ix[x]['class'] = readable[labels.index(data.ix[x]['class'])]
    return data
    # dframe = pd.read_csv(file_, names=["t", "SSID", "RSSI", "class"] + list(wifi_hash))
    # for x in xrange(0, len(dframe)):dframe.set_value(x, dframe.ix[x]['SSID'], dframe.ix[x]['RSSI'])
    # dframe.drop([INDEX_COLUMN] + ["SSID", "RSSI"], axis=1).fillna('?').to_csv('raw_output_arjun2.csv', encoding='utf-8', index=False)

if __name__ == "__main__":

    # Build test file
    wifi_hash = set([])
    raw_test_dataframe = pd.concat(((importFile)(f) for f in test_data_dir), ignore_index=True)
    test_feature_dframe = buildFeatureList(raw_test_dataframe)
    test_feature_dframe.drop([INDEX_COLUMN] + ["SSID", "RSSI"], axis=1).fillna('?').sort_values(['class'], axis=0).to_csv('test.csv', encoding='utf-8', index=False)
    print 'Built test-hash with', len(wifi_hash), 'unique entries'

    # Build training file
    wifi_hash = set([])
    raw_training_dataframe = pd.concat(((importFile)(f) for f in train_data_dir), ignore_index=True)
    training_feature_dframe = buildFeatureList(raw_training_dataframe)
    training_feature_dframe.drop([INDEX_COLUMN] + ["SSID", "RSSI"], axis=1).fillna('?').to_csv('train.csv', encoding='utf-8', index=False)
    print 'Built training-hash with', len(wifi_hash), 'unique entries'

    exit()


## Start building raw-output dataframe
dframe = pd.read_csv(open(_INPUT_FILE_), names=[INDEX_COLUMN] + ["SSID", "RSSI"] +[LABEL_COLUMN] + list(wifi_hash))#, index_col=INDEX_COLUMN)
for x in xrange(0, len(dframe)):dframe.set_value(x, dframe.ix[x]['SSID'], dframe.ix[x]['RSSI'])
dframe.drop([INDEX_COLUMN] + ["SSID", "RSSI"], axis=1).fillna('?').to_csv('raw_output_arjun2.csv', encoding='utf-8', index=False)

# SSID   = [str(i.split(',')[1]) for i in open(_INPUT_FILE_).readlines()]
# dframe = pd.read_csv(file_, names=["t", "SSID", "RSSI", "class"] + list(wifi_hash))
# for x in xrange(0, len(dframe)):dframe.set_value(x, dframe.ix[x]['SSID'], dframe.ix[x]['RSSI'])
# dframe.drop([INDEX_COLUMN] + ["SSID", "RSSI"], axis=1).fillna('?').to_csv('raw_output_arjun2.csv', encoding='utf-8', index=False)
