/*
 * set_params.h
 *
 *  Created on: 2013/11/23
 *      Author: Owner
 */


#ifndef STRUCTS_H_
#include  "structs.h"
#define STRUCTS_H_
#endif


#ifndef SET_PARAMS_H_
#define MPA_MONO    3
int alloc_video_context(AVCodecContext* avctx);
int alloc_mp3_codeccontext(AVCodecContext *avctx);

//
//#define av_bswap16 av_bswap16
//static inline const unsigned av_bswap16(unsigned x)
//{
//    __asm__("rorw $8, %w0" : "+r"(x));
//    return x;
//}
//
//#define av_bswap32 av_bswap32
//static inline const uint32_t av_bswap32(uint32_t x)
//{
//#if HAVE_BSWAP
//    __asm__("bswap   %0" : "+r" (x));
//#else
//    __asm__("rorw    $8,  %w0 \n\t"
//            "rorl    $16, %0  \n\t"
//            "rorw    $8,  %w0"
//            : "+r"(x));
//#endif
//    return x;
//}
//
//#if ARCH_X86_64
//#define av_bswap64 av_bswap64
//static inline uint64_t av_const av_bswap64(uint64_t x)
//{
//    __asm__("bswap  %0": "=r" (x) : "0" (x));
//    return x;
//}
//#endif


int startenc(JNIEnv* env,int flag);

int audio_encode_example(JNIEnv* env,AVCodec* codec);

int video_encode_example(JNIEnv* env,AVCodec* codec);

#define SET_PARAMS_H_



#endif /* SET_PARAMS_H_ */
