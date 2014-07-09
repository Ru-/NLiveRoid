/*
 * h263_encoder_init.h
 *
 *  Created on: 2013/11/17
 *      Author: Owner
 */

#ifndef UTIL_H_
#include "util.h"
#define UTIL_H_
#endif

uint8_t ff_h263_static_rl_table_store[2][2][2*MAX_RUN + MAX_LEVEL + 3];


extern const uint8_t ff_alternate_horizontal_scan[64] ;


extern const int16_t ff_mpeg4_default_intra_matrix[64];

extern const uint16_t ff_mpeg1_default_non_intra_matrix[64] ;


extern const uint16_t ff_h263_format[8][2];


extern const int16_t ff_mpeg4_default_non_intra_matrix[64];

extern const uint8_t ff_h263_chroma_qscale_table[32];

extern const uint16_t ff_aanscales[64];

void h263_encode_init(MpegEncContext *s);
