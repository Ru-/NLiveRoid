
#include <string.h>

#ifndef UTIL_H_
#include "util.h"
#define UTIL_H_
#endif

#include "encode.h"
#include "get_params.h"

#include <android/log.h>
#define LOG_TAG "NLiveRoid"
#define LOGD(...)  __android_log_print(3, LOG_TAG, __VA_ARGS__)

int get_linesizes(int linesize_[4],int width_,enum AVPixelFormat  pix_fmt){

	LOGD("get_linesize Called:%d\n");
	int i,width,fail = 0,result,align = 32;
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);

    int max_step     [4];       /* max pixel step for each plane */
    int max_step_comp[4];       /* the component for each plane which has the max pixel step */

    memset(linesize_, 0, 4*sizeof(linesize_[0]));

	LOGD("get_linesize align:%d\n",align);
    for(i=1; i<=align; i+=i) {//align回回すけど実際1回しかしてない
        width = FFALIGN(width_, i);
        av_image_fill_max_pixsteps(max_step, max_step_comp, desc);
        for (i = 0; i < 4; i++) {
    			if ((result = image_get_linesize(width, i, max_step[i], max_step_comp[i], desc)) < 0){
    				LOGD("Wrong linesize");
    				return -1;
    			}
    			linesize_[i] = result;
    			fail++;
    			if(fail > 200)return -3;
    		LOGD("set_linesize linesize[%d]:%d",i,linesize_[i]);
        }
        if (!(linesize_[0] & (align-1)))
            break;
    }
    for (i = 0; i < 4; i++){
    	linesize_[i] = FFALIGN(linesize_[i], align);
    }
    return 0;
}


enum var_name {
    VAR_FRAME_RATE,
    VAR_INTERLACED,
    VAR_N,
    VAR_NB_CONSUMED_SAMPLES,
    VAR_NB_SAMPLES,
    VAR_POS,
    VAR_PREV_INPTS,
    VAR_PREV_INT,
    VAR_PREV_OUTPTS,
    VAR_PREV_OUTT,
    VAR_PTS,
    VAR_SAMPLE_RATE,
    VAR_STARTPTS,
    VAR_STARTT,
    VAR_T,
    VAR_TB,
    VAR_RTCTIME,
    VAR_RTCSTART,
    VAR_S,
    VAR_SR,
    VAR_VARS_NB
};
/*var_name
0    VAR_FRAME_RATE 			15固定
1    VAR_INTERLACED 			ずっと0
2    VAR_N						frame_numberと一致? 0,1,2,3 ..
3    VAR_NB_CONSUMED_SAMPLES 	ずっと0
4    VAR_NB_SAMPLES,			ずっと0
5    VAR_POS					解像度*3/2づつ増えていく(480*320の場合は230400づつ増えている)
6    VAR_PREV_INPTS,			nanから始まって0,1,2,3 ..
7    VAR_PREV_INT,				nanから始まってPTSのfloat表示
8    VAR_PREV_OUTPTS			nanから始まって0,1,2,3 ..
9    VAR_PREV_OUTT,				nanから始まってPTSのfloat表示
10    VAR_PTS,					nanから始まって0,1,2,3 ..
11    VAR_SAMPLE_RATE,			ずっとnan
12    VAR_STARTPTS,				ずっと0
13    VAR_STARTT,				ずっと0
14    VAR_T,					PTSのfloat表示
15    VAR_TB,					PTSの1つ分のfloat表示(480*320だったら0.066667)
16    VAR_RTCTIME,				RTC時間?16桁数値で3～40000くらいの不規則な差がある
17    VAR_RTCSTART				RTC開始時間?16桁の数値で最初のVAR_RTCTIMEと2000位の差で始まり最後のVAR_RTCTIMEと320000位の差
18    VAR_S						ずっと0
19    VAR_SR,					ずっとnan
20    VAR_VARS_NB				ずっと0
 *
 */
double var_values[VAR_VARS_NB];
//フレーム番号からPTSを設定する
int set_video_pts(int frame_number,AVRational frame_rate){

		LOGD("set_video_pts Called frame_number:%d\n",frame_number);
	    int type = AVMEDIA_TYPE_VIDEO;
	    int nb_samples = 44100;

	    if (isnan(var_values[VAR_STARTPTS])) {
	    	LOGD("setpts isnan TRUE\n");
	        var_values[VAR_STARTPTS] = TS2D(frame_number);
	        var_values[VAR_STARTT  ] = TS2T(frame_number, frame_rate);
	    }
	    var_values[VAR_PTS       ] = TS2D(frame_number);
	    var_values[VAR_T         ] = TS2T(frame_number, frame_rate);
//	    var_values[VAR_POS       ] = av_frame_get_pkt_pos(frame) == -1 ? NAN : av_frame_get_pkt_pos(frame);
	    var_values[VAR_RTCTIME   ] = av_gettime();

	    if (type == AVMEDIA_TYPE_AUDIO) {
	        var_values[VAR_S] = nb_samples;
	        var_values[VAR_NB_SAMPLES] = nb_samples;
	    }

		int i;
		for(i = 0; i <= VAR_VARS_NB ; i++){
			if(i % 5 == 0)
				LOGD("\n");
			LOGD("%f ",var_values[i]);
		}
		LOGD("\n");

	    // inlink->timebase den15 num1
	    if (type == AVMEDIA_TYPE_VIDEO) {
	        var_values[VAR_N] += 1.0;
	    } else {
	        var_values[VAR_N] += nb_samples;
	    }

	    LOGD("set_video_pts PTS:%f",TS2T(frame_number, frame_rate));

	    var_values[VAR_PREV_INPTS ] = TS2D(frame_number);
	    var_values[VAR_PREV_INT   ] = TS2T(frame_number, frame_rate);
	    var_values[VAR_PREV_OUTPTS] = TS2D(frame_number);
	    var_values[VAR_PREV_OUTT]   = TS2T(frame_number, frame_rate);//結局この値以外別にいらない
	    if (type == AVMEDIA_TYPE_AUDIO) {
	        var_values[VAR_NB_CONSUMED_SAMPLES] += nb_samples;//AUDIOの場合ここでサンプルレート分増加する?(使用されているか未確認)
	    }
	    return 0;

}


