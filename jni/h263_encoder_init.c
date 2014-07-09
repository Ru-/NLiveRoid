/*
 * h263_encoder_init.c
 *
 *  Created on: 2013/11/17
 *      Author: Owner
 */

#ifndef H263_ENCODER_H_
#define H263_ENCODER_H_
#include "h263_encoder_init.h"
#endif

#include <android/log.h>
#define LOG_TAG "NLiveRoid"
#define LOGD(...)  __android_log_print(3, LOG_TAG, __VA_ARGS__)

//このへんは実体はよーわからんが、宣言しておかないとビルドが通らない
/**
 * Table of number of bits a motion vector component needs.
 */
static uint8_t mv_penalty[MAX_FCODE+1][MAX_MV*2+1];
/**
 * Minimal fcode that a motion vector component would need.
 */
static uint8_t fcode_tab[MAX_MV*2+1];

/**
 * Minimal fcode that a motion vector component would need in umv.
 * All entries in this table are 1.
 */
static uint8_t umv_fcode_tab[MAX_MV*2+1];

//unified encoding tables for run length encoding of coefficients
//unified in the sense that the specification specifies the encoding in several steps.
static uint8_t  uni_h263_intra_aic_rl_len [64*64*2*2];
static uint8_t  uni_h263_inter_rl_len [64*64*2*2];

uint8_t ff_h263_static_rl_table_store[2][2][2*MAX_RUN + MAX_LEVEL + 3];

const uint8_t ff_alternate_horizontal_scan[64] = {
    0,  1,   2,  3,  8,  9, 16, 17,
    10, 11,  4,  5,  6,  7, 15, 14,
    13, 12, 19, 18, 24, 25, 32, 33,
    26, 27, 20, 21, 22, 23, 28, 29,
    30, 31, 34, 35, 40, 41, 48, 49,
    42, 43, 36, 37, 38, 39, 44, 45,
    46, 47, 50, 51, 56, 57, 58, 59,
    52, 53, 54, 55, 60, 61, 62, 63,
};

const int16_t ff_mpeg4_default_intra_matrix[64] = {
 8, 17, 18, 19, 21, 23, 25, 27,
17, 18, 19, 21, 23, 25, 27, 28,
20, 21, 22, 23, 24, 26, 28, 30,
21, 22, 23, 24, 26, 28, 30, 32,
22, 23, 24, 26, 28, 30, 32, 35,
23, 24, 26, 28, 30, 32, 35, 38,
25, 26, 28, 30, 32, 35, 38, 41,
27, 28, 30, 32, 35, 38, 41, 45,
};



const uint16_t ff_mpeg1_default_non_intra_matrix[64]  = {
    16, 16, 16, 16, 16, 16, 16, 16,
    16, 16, 16, 16, 16, 16, 16, 16,
    16, 16, 16, 16, 16, 16, 16, 16,
    16, 16, 16, 16, 16, 16, 16, 16,
    16, 16, 16, 16, 16, 16, 16, 16,
    16, 16, 16, 16, 16, 16, 16, 16,
    16, 16, 16, 16, 16, 16, 16, 16,
    16, 16, 16, 16, 16, 16, 16, 16,
};






const int16_t ff_mpeg4_default_non_intra_matrix[64] = {
16, 17, 18, 19, 20, 21, 22, 23,
17, 18, 19, 20, 21, 22, 23, 24,
18, 19, 20, 21, 22, 23, 24, 25,
19, 20, 21, 22, 23, 24, 26, 27,
20, 21, 22, 23, 25, 26, 27, 28,
21, 22, 23, 24, 26, 27, 28, 30,
22, 23, 24, 26, 27, 28, 30, 31,
23, 24, 25, 27, 28, 30, 31, 33,
};



const uint8_t ff_h263_chroma_qscale_table[32] ={
//  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
    0, 1, 2, 3, 4, 5, 6, 6, 7, 8, 9, 9,10,10,11,11,12,12,12,13,13,13,14,14,14,14,14,15,15,15,15,15
};



const uint16_t ff_aanscales[64] = {
    /* precomputed values scaled up by 14 bits */
    16384, 22725, 21407, 19266, 16384, 12873,  8867,  4520,
    22725, 31521, 29692, 26722, 22725, 17855, 12299,  6270,
    21407, 29692, 27969, 25172, 21407, 16819, 11585,  5906,
    19266, 26722, 25172, 22654, 19266, 15137, 10426,  5315,
    16384, 22725, 21407, 19266, 16384, 12873,  8867,  4520,
    12873, 17855, 16819, 15137, 12873, 10114,  6967,  3552,
    8867 , 12299, 11585, 10426,  8867,  6967,  4799,  2446,
    4520 ,  6270,  5906,  5315,  4520,  3552,  2446,  1247
};













static void init_mv_penalty_and_fcode(MpegEncContext *s)
{
    int f_code;
    int mv;

    for(f_code=1; f_code<=MAX_FCODE; f_code++){
        for(mv=-MAX_MV; mv<=MAX_MV; mv++){
            int len;

            if(mv==0) len= ff_mvtab[0][1];
            else{
                int val, bit_size, code;

                bit_size = f_code - 1;

                val=mv;
                if (val < 0)
                    val = -val;
                val--;
                code = (val >> bit_size) + 1;
                if(code<33){
                    len= ff_mvtab[code][1] + 1 + bit_size;
                }else{
                    len= ff_mvtab[32][1] + av_log2(code>>5) + 2 + bit_size;
                }
            }

            mv_penalty[f_code][mv+MAX_MV]= len;
        }
    }

    for(f_code=MAX_FCODE; f_code>0; f_code--){
        for(mv=-(16<<f_code); mv<(16<<f_code); mv++){
            fcode_tab[mv+MAX_MV]= f_code;
        }
    }

    for(mv=0; mv<MAX_MV*2+1; mv++){
        umv_fcode_tab[mv]= 1;
    }
}

void ff_init_rl(RLTable *rl,
                uint8_t static_store[2][2 * MAX_RUN + MAX_LEVEL + 3])
{
    int8_t  max_level[MAX_RUN + 1], max_run[MAX_LEVEL + 1];
    uint8_t index_run[MAX_RUN + 1];
    int last, run, level, start, end, i;

    /* If table is static, we can quit if rl->max_level[0] is not NULL */
    if (static_store && rl->max_level[0])
        return;

    /* compute max_level[], max_run[] and index_run[] */
    for (last = 0; last < 2; last++) {
        if (last == 0) {
            start = 0;
            end = rl->last;
        } else {
            start = rl->last;
            end = rl->n;
        }

        memset(max_level, 0, MAX_RUN + 1);
        memset(max_run, 0, MAX_LEVEL + 1);
        memset(index_run, rl->n, MAX_RUN + 1);
        for (i = start; i < end; i++) {
            run   = rl->table_run[i];
            level = rl->table_level[i];
            if (index_run[run] == rl->n)
                index_run[run] = i;
            if (level > max_level[run])
                max_level[run] = level;
            if (run > max_run[level])
                max_run[level] = run;
        }
        if (static_store)
            rl->max_level[last] = static_store[last];
        else
            rl->max_level[last] = av_malloc(MAX_RUN + 1);
        memcpy(rl->max_level[last], max_level, MAX_RUN + 1);
        if (static_store)
            rl->max_run[last]   = static_store[last] + MAX_RUN + 1;
        else
            rl->max_run[last]   = av_malloc(MAX_LEVEL + 1);
        memcpy(rl->max_run[last], max_run, MAX_LEVEL + 1);
        if (static_store)
            rl->index_run[last] = static_store[last] + MAX_RUN + MAX_LEVEL + 2;
        else
            rl->index_run[last] = av_malloc(MAX_RUN + 1);
        memcpy(rl->index_run[last], index_run, MAX_RUN + 1);
    }
}


static inline int get_rl_index(const RLTable *rl, int last, int run, int level)
{
    int index;
    index = rl->index_run[last][run];
    if (index >= rl->n)
        return rl->n;
    if (level > rl->max_level[last][run])
        return rl->n;
    return index + level - 1;
}


static void init_uni_h263_rl_tab(RLTable *rl, uint32_t *bits_tab, uint8_t *len_tab){
    int slevel, run, last;

//    av_assert0(MAX_LEVEL >= 64);
//    av_assert0(MAX_RUN   >= 63);

    for(slevel=-64; slevel<64; slevel++){
        if(slevel==0) continue;
        for(run=0; run<64; run++){
            for(last=0; last<=1; last++){
                const int index= UNI_MPEG4_ENC_INDEX(last, run, slevel+64);
                int level= slevel < 0 ? -slevel : slevel;
                int sign= slevel < 0 ? 1 : 0;
                int bits, len, code;

                len_tab[index]= 100;

                /* ESC0 */
                code= get_rl_index(rl, last, run, level);
                bits= rl->table_vlc[code][0];
                len=  rl->table_vlc[code][1];
                bits=bits*2+sign; len++;

                if(code!=rl->n && len < len_tab[index]){
                    if(bits_tab) bits_tab[index]= bits;
                    len_tab [index]= len;
                }
                /* ESC */
                bits= rl->table_vlc[rl->n][0];
                len = rl->table_vlc[rl->n][1];
                bits=bits*2+last; len++;
                bits=bits*64+run; len+=6;
                bits=bits*256+(level&0xff); len+=8;

                if(len < len_tab[index]){
                    if(bits_tab) bits_tab[index]= bits;
                    len_tab [index]= len;
                }
            }
        }
    }
}


void h263_encode_init(MpegEncContext *s)
{
    static int done = 0;

    if (!done) {
        done = 1;

        ff_init_rl(&ff_h263_rl_inter, ff_h263_static_rl_table_store[0]);
        ff_init_rl(&ff_rl_intra_aic, ff_h263_static_rl_table_store[1]);

        init_uni_h263_rl_tab(&ff_rl_intra_aic, NULL, uni_h263_intra_aic_rl_len);
        init_uni_h263_rl_tab(&ff_h263_rl_inter    , NULL, uni_h263_inter_rl_len);

        init_mv_penalty_and_fcode(s);
    }
    s->me.mv_penalty= mv_penalty; //FIXME exact table for msmpeg4 & h263p

    s->intra_ac_vlc_length     =s->inter_ac_vlc_length     = uni_h263_inter_rl_len;
    s->intra_ac_vlc_last_length=s->inter_ac_vlc_last_length= uni_h263_inter_rl_len + 128*64;
    if(s->h263_aic){
        s->intra_ac_vlc_length     = uni_h263_intra_aic_rl_len;
        s->intra_ac_vlc_last_length= uni_h263_intra_aic_rl_len + 128*64;
    }
    s->ac_esc_length= 7+1+6+8;

    // use fcodes >1 only for mpeg4 & h263 & h263p FIXME
    switch(s->codec_id){
    case AV_CODEC_ID_MPEG4:
        s->fcode_tab= fcode_tab;
        break;
    case AV_CODEC_ID_H263P:
        if(s->umvplus)
            s->fcode_tab= umv_fcode_tab;
        if(s->modified_quant){
            s->min_qcoeff= -2047;
            s->max_qcoeff=  2047;
        }else{
            s->min_qcoeff= -127;
            s->max_qcoeff=  127;
        }
        break;
        //Note for mpeg4 & h263 the dc-scale table will be set per frame as needed later
    case AV_CODEC_ID_FLV1:
        if (s->h263_flv > 1) {
            s->min_qcoeff= -1023;
            s->max_qcoeff=  1023;
        } else {
            s->min_qcoeff= -127;
            s->max_qcoeff=  127;
        }
        s->y_dc_scale_table=
        s->c_dc_scale_table= ff_mpeg1_dc_scale_table;
        break;
    default: //nothing needed - default table already set in mpegvideo.c
        s->min_qcoeff= -127;
        s->max_qcoeff=  127;
        s->y_dc_scale_table=
        s->c_dc_scale_table= ff_mpeg1_dc_scale_table;
    }
}
