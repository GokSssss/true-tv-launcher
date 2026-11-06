#!/usr/bin/env python3
"""Original vector graphics: simple pixel letters and outline squares. No font assets."""
from pathlib import Path
letters={
'T':["11111","00100","00100","00100","00100","00100","00100"],
'R':["11110","10001","10001","11110","10100","10010","10001"],
'U':["10001","10001","10001","10001","10001","10001","01110"],
'E':["11111","10000","10000","11110","10000","10000","11111"],
'V':["10001","10001","10001","10001","01010","01010","00100"],
'L':["10000","10000","10000","10000","10000","10000","11111"],
'A':["01110","10001","10001","11111","10001","10001","10001"],
'N':["10001","11001","11001","10101","10011","10011","10001"],
'C':["01111","10000","10000","10000","10000","10000","01111"],
'H':["10001","10001","10001","11111","10001","10001","10001"]}
def text(word,y,scale):
    paths=[];x=(108-(len(word)*6-1)*scale)/2
    for ch in word:
        for row,line in enumerate(letters.get(ch,["00000"]*7)):
            for col,bit in enumerate(line):
                if bit=='1':
                    xx=x+col*scale; yy=y+row*scale
                    paths.append(f'M{xx:g},{yy:g}h{scale:g}v{scale:g}h-{scale:g}z')
        x+=6*scale
    return ''.join(paths)
base='<path android:fillColor="#E9CA70" android:pathData="M16,0H92Q108,0 108,16V92Q108,108 92,108H16Q0,108 0,92V16Q0,0 16,0z"/>'
for y in (12,86):
    for x in (16,33,50,67,84): base+=f'<path android:fillColor="@android:color/transparent" android:strokeColor="#28251F" android:strokeWidth="1.2" android:pathData="M{x+1.5},{y}h6q1.5,0 1.5,1.5v6q0,1.5 -1.5,1.5h-6q-1.5,0 -1.5,-1.5v-6q0,-1.5 1.5,-1.5z"/>'
base+=f'<path android:fillColor="#171714" android:pathData="{text("TRUE TV",34,2)}{text("LAUNCHER",57,1.8)}"/>'
header='<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">'
p=Path('app/src/main/res/drawable');(p/'launcher_icon.xml').write_text(header+base+'</vector>')
(p/'launcher_banner.xml').write_text('<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="320dp" android:height="180dp" android:viewportWidth="192" android:viewportHeight="108"><path android:fillColor="#E9CA70" android:pathData="M0,0h192v108H0z"/><group android:translateX="42">'+base+'</group></vector>')
