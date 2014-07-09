/*
 * codec_base.h
 *
 *  Created on: 2013/11/20
 *      Author: Owner
 */


#ifndef UTIL_H_
#define UTIL_H_
#include "util.h"
#endif


#define CODEC_FLAG_TRUNCATED       0x00010000 /** Input bitstream might be truncated at a random
                                                  location instead of only at frame boundaries. */

/**
 * Codec supports lossy compression. Audio and video codecs only.
 * @note a codec may support both lossy and lossless
 * compression modes
 */
#define AV_CODEC_PROP_LOSSY         (1 << 1)

/**
 * Codec uses only intra compression.
 * Video codecs only.
 */
#define AV_CODEC_PROP_INTRA_ONLY    (1 << 0)
/**
 * Codec supports lossless compression. Audio and video codecs only.
 */
#define AV_CODEC_PROP_LOSSLESS      (1 << 2)

#define CONFIG_ICONV 0

extern AVCodecDescriptor codec_descriptors[];


inline AVCodec *avcodec_find_encoder(enum AVCodecID id);
AVCodecContext *avcodec_alloc_context3();


/**
 * @defgroup channel_masks Audio channel masks
 *
 * A channel layout is a 64-bits integer with a bit set for every channel.
 * The number of bits set must be equal to the number of channels.
 * The value 0 means that the channel layout is not known.
 * @note this data structure is not powerful enough to handle channels
 * combinations that have the same channel multiple times, such as
 * dual-mono.
 *
 * @{
 */
#define AV_CH_LOW_FREQUENCY          0x00000008
#define AV_CH_BACK_LEFT              0x00000010
#define AV_CH_BACK_RIGHT             0x00000020
#define AV_CH_FRONT_LEFT_OF_CENTER   0x00000040
#define AV_CH_FRONT_RIGHT_OF_CENTER  0x00000080
#define AV_CH_BACK_CENTER            0x00000100
#define AV_CH_SIDE_LEFT              0x00000200
#define AV_CH_SIDE_RIGHT             0x00000400
#define AV_CH_TOP_CENTER             0x00000800
#define AV_CH_TOP_FRONT_LEFT         0x00001000
#define AV_CH_TOP_FRONT_CENTER       0x00002000
#define AV_CH_TOP_FRONT_RIGHT        0x00004000
#define AV_CH_TOP_BACK_LEFT          0x00008000
#define AV_CH_TOP_BACK_CENTER        0x00010000
#define AV_CH_TOP_BACK_RIGHT         0x00020000
#define AV_CH_STEREO_LEFT            0x20000000  ///< Stereo downmix.
#define AV_CH_STEREO_RIGHT           0x40000000  ///< See AV_CH_STEREO_LEFT.
#define AV_CH_WIDE_LEFT              0x0000000080000000ULL
#define AV_CH_WIDE_RIGHT             0x0000000100000000ULL
#define AV_CH_SURROUND_DIRECT_LEFT   0x0000000200000000ULL
#define AV_CH_SURROUND_DIRECT_RIGHT  0x0000000400000000ULL
#define AV_CH_LOW_FREQUENCY_2        0x0000000800000000ULL

/**
 * @}
 * @defgroup channel_mask_c Audio channel convenience macros
 * @{
// * */
//#define AV_CH_LAYOUT_MONO              (AV_CH_FRONT_CENTER)
//#define AV_CH_LAYOUT_STEREO            (AV_CH_FRONT_LEFT|AV_CH_FRONT_RIGHT)
#define AV_CH_LAYOUT_2POINT1           (AV_CH_LAYOUT_STEREO|AV_CH_LOW_FREQUENCY)
#define AV_CH_LAYOUT_2_1               (AV_CH_LAYOUT_STEREO|AV_CH_BACK_CENTER)
#define AV_CH_LAYOUT_SURROUND          (AV_CH_LAYOUT_STEREO|AV_CH_FRONT_CENTER)
#define AV_CH_LAYOUT_3POINT1           (AV_CH_LAYOUT_SURROUND|AV_CH_LOW_FREQUENCY)
#define AV_CH_LAYOUT_4POINT0           (AV_CH_LAYOUT_SURROUND|AV_CH_BACK_CENTER)
#define AV_CH_LAYOUT_4POINT1           (AV_CH_LAYOUT_4POINT0|AV_CH_LOW_FREQUENCY)
#define AV_CH_LAYOUT_2_2               (AV_CH_LAYOUT_STEREO|AV_CH_SIDE_LEFT|AV_CH_SIDE_RIGHT)
#define AV_CH_LAYOUT_QUAD              (AV_CH_LAYOUT_STEREO|AV_CH_BACK_LEFT|AV_CH_BACK_RIGHT)
#define AV_CH_LAYOUT_5POINT0           (AV_CH_LAYOUT_SURROUND|AV_CH_SIDE_LEFT|AV_CH_SIDE_RIGHT)
#define AV_CH_LAYOUT_5POINT1           (AV_CH_LAYOUT_5POINT0|AV_CH_LOW_FREQUENCY)
#define AV_CH_LAYOUT_5POINT0_BACK      (AV_CH_LAYOUT_SURROUND|AV_CH_BACK_LEFT|AV_CH_BACK_RIGHT)
#define AV_CH_LAYOUT_5POINT1_BACK      (AV_CH_LAYOUT_5POINT0_BACK|AV_CH_LOW_FREQUENCY)
#define AV_CH_LAYOUT_6POINT0           (AV_CH_LAYOUT_5POINT0|AV_CH_BACK_CENTER)
#define AV_CH_LAYOUT_6POINT0_FRONT     (AV_CH_LAYOUT_2_2|AV_CH_FRONT_LEFT_OF_CENTER|AV_CH_FRONT_RIGHT_OF_CENTER)
#define AV_CH_LAYOUT_HEXAGONAL         (AV_CH_LAYOUT_5POINT0_BACK|AV_CH_BACK_CENTER)
#define AV_CH_LAYOUT_6POINT1           (AV_CH_LAYOUT_5POINT1|AV_CH_BACK_CENTER)
#define AV_CH_LAYOUT_6POINT1_BACK      (AV_CH_LAYOUT_5POINT1_BACK|AV_CH_BACK_CENTER)
#define AV_CH_LAYOUT_6POINT1_FRONT     (AV_CH_LAYOUT_6POINT0_FRONT|AV_CH_LOW_FREQUENCY)
#define AV_CH_LAYOUT_7POINT0           (AV_CH_LAYOUT_5POINT0|AV_CH_BACK_LEFT|AV_CH_BACK_RIGHT)
#define AV_CH_LAYOUT_7POINT0_FRONT     (AV_CH_LAYOUT_5POINT0|AV_CH_FRONT_LEFT_OF_CENTER|AV_CH_FRONT_RIGHT_OF_CENTER)
#define AV_CH_LAYOUT_7POINT1           (AV_CH_LAYOUT_5POINT1|AV_CH_BACK_LEFT|AV_CH_BACK_RIGHT)
#define AV_CH_LAYOUT_7POINT1_WIDE      (AV_CH_LAYOUT_5POINT1|AV_CH_FRONT_LEFT_OF_CENTER|AV_CH_FRONT_RIGHT_OF_CENTER)
#define AV_CH_LAYOUT_7POINT1_WIDE_BACK (AV_CH_LAYOUT_5POINT1_BACK|AV_CH_FRONT_LEFT_OF_CENTER|AV_CH_FRONT_RIGHT_OF_CENTER)
#define AV_CH_LAYOUT_OCTAGONAL         (AV_CH_LAYOUT_5POINT0|AV_CH_BACK_LEFT|AV_CH_BACK_CENTER|AV_CH_BACK_RIGHT)
#define AV_CH_LAYOUT_STEREO_DOWNMIX    (AV_CH_STEREO_LEFT|AV_CH_STEREO_RIGHT)

#define av_bprint_room(buf) ((buf)->size - FFMIN((buf)->len, (buf)->size))


#define AVERROR_INVALIDDATA        FFERRTAG( 'I','N','D','A') ///< Invalid data found when processing input


#define UINT_MAX (INT_MAX * 2U + 1)

#define av_bprint_is_allocated(buf) ((buf)->str != (buf)->reserved_internal_buffer)




/* JPEG marker codes */
typedef enum {
    /* start of frame */
    SOF0  = 0xc0,       /* baseline */
    SOF1  = 0xc1,       /* extended sequential, huffman */
    SOF2  = 0xc2,       /* progressive, huffman */
    SOF3  = 0xc3,       /* lossless, huffman */

    SOF5  = 0xc5,       /* differential sequential, huffman */
    SOF6  = 0xc6,       /* differential progressive, huffman */
    SOF7  = 0xc7,       /* differential lossless, huffman */
    JPG   = 0xc8,       /* reserved for JPEG extension */
    SOF9  = 0xc9,       /* extended sequential, arithmetic */
    SOF10 = 0xca,       /* progressive, arithmetic */
    SOF11 = 0xcb,       /* lossless, arithmetic */

    SOF13 = 0xcd,       /* differential sequential, arithmetic */
    SOF14 = 0xce,       /* differential progressive, arithmetic */
    SOF15 = 0xcf,       /* differential lossless, arithmetic */

    DHT   = 0xc4,       /* define huffman tables */

    DAC   = 0xcc,       /* define arithmetic-coding conditioning */

    /* restart with modulo 8 count "m" */
    RST0  = 0xd0,
    RST1  = 0xd1,
    RST2  = 0xd2,
    RST3  = 0xd3,
    RST4  = 0xd4,
    RST5  = 0xd5,
    RST6  = 0xd6,
    RST7  = 0xd7,

    SOI   = 0xd8,       /* start of image */
    EOI   = 0xd9,       /* end of image */
    SOS   = 0xda,       /* start of scan */
    DQT   = 0xdb,       /* define quantization tables */
    DNL   = 0xdc,       /* define number of lines */
    DRI   = 0xdd,       /* define restart interval */
    DHP   = 0xde,       /* define hierarchical progression */
    EXP   = 0xdf,       /* expand reference components */

    APP0  = 0xe0,
    APP1  = 0xe1,
    APP2  = 0xe2,
    APP3  = 0xe3,
    APP4  = 0xe4,
    APP5  = 0xe5,
    APP6  = 0xe6,
    APP7  = 0xe7,
    APP8  = 0xe8,
    APP9  = 0xe9,
    APP10 = 0xea,
    APP11 = 0xeb,
    APP12 = 0xec,
    APP13 = 0xed,
    APP14 = 0xee,
    APP15 = 0xef,

    JPG0  = 0xf0,
    JPG1  = 0xf1,
    JPG2  = 0xf2,
    JPG3  = 0xf3,
    JPG4  = 0xf4,
    JPG5  = 0xf5,
    JPG6  = 0xf6,
    SOF48 = 0xf7,       ///< JPEG-LS
    LSE   = 0xf8,       ///< JPEG-LS extension parameters
    JPG9  = 0xf9,
    JPG10 = 0xfa,
    JPG11 = 0xfb,
    JPG12 = 0xfc,
    JPG13 = 0xfd,

    COM   = 0xfe,       /* comment */

    TEM   = 0x01,       /* temporary private use for arithmetic coding */

    /* 0x02 -> 0xbf reserved */
} JPEG_MARKER;


#define FLAG_QPEL   1 //must be 1

#define P_LEFT P[1]
#define P_TOP P[2]
#define P_TOPRIGHT P[3]
#define P_MEDIAN P[4]
#define P_MV1 P[9]


const uint8_t sp5x_quant_table[20][64];

//このマクロの代わり?にlibavutil/tomi/inteadwrite.hに、
//av_always_inlineの関数があって、コンパイルじにそんな命令ないみたいな事を言われて通らない+このマクロも通らない
//#define AV_WN(s, p, v) ((((union unaligned_##s *) (p))->l) = (v))
//#define AV_WB32(s, p, v) AV_?WN##s(p, av_bswap##s(v))


//Let us hope gcc will remove the unused vars ...(gcc 3.2.2 seems to do it ...)
#define LOAD_COMMON\
    uint32_t __attribute__((unused)) * const score_map= c->score_map;\
    const int __attribute__((unused)) xmin= c->xmin;\
    const int __attribute__((unused)) ymin= c->ymin;\
    const int __attribute__((unused)) xmax= c->xmax;\
    const int __attribute__((unused)) ymax= c->ymax;\
    uint8_t *mv_penalty= c->current_mv_penalty;\
    const int pred_x= c->pred_x;\
    const int pred_y= c->pred_y;\

#define LOAD_COMMON2\
    uint32_t *map= c->map;\
    const int qpel= flags&FLAG_QPEL;\
    const int shift= 1+qpel;\


    #define FLAG_DIRECT 4

#define II_BITRATE 128*1024
#define MBAC_BITRATE 50*1024

#define NB_RL_TABLES  6
#define CODEC_FLAG_GLOBAL_HEADER  0x00400000 ///< Place global headers in extradata instead of every keyframe.

#define VOP_STARTCODE        0x1B6
#define GMC_SPRITE 2
#define VOS_STARTCODE        0x1B0
#define VISUAL_OBJ_STARTCODE 0x1B5
#define ADV_SIMPLE_VO_TYPE         17
#define SIMPLE_VO_TYPE             1
#define RECT_SHAPE       0
#define LIBAVCODEC_IDENT        "Lavc TEST MPEG4"


#define COPY3_IF_LT(x, y, a, b, c, d)\
if ((y) < (x)) {\
    (x) = (y);\
    (a) = (b);\
    (c) = (d);\
}

#define CHECK_SAD_HALF_MV(suffix, x, y) \
{\
    d= s->dsp.pix_abs[size][(x?1:0)+(y?2:0)](NULL, pix, ptr+((x)>>1), stride, h);\
    d += (mv_penalty[pen_x + x] + mv_penalty[pen_y + y])*penalty_factor;\
    COPY3_IF_LT(dminh, d, dx, x, dy, y)\
}

#define FFUDIV(a,b) (((a)>0 ?(a):(a)-(b)+1) / (b))
#define FFUMOD(a,b) ((a)-(b)*FFUDIV(a,b))
#define GOP_STARTCODE        0x1B3
#define CODEC_FLAG_NORMALIZE_AQP  0x00020000 ///< Normalize adaptive quantization.

#define CHECK_MV(x,y)\
{\
    const unsigned key = ((y)<<ME_MAP_MV_BITS) + (x) + map_generation;\
    const int index= (((y)<<ME_MAP_SHIFT) + (x))&(ME_MAP_SIZE-1);\
    if(map[index]!=key){\
        d= cmp(s, x, y, 0, 0, size, h, ref_index, src_index, cmpf, chroma_cmpf, flags);\
        map[index]= key;\
        score_map[index]= d;\
        d += (mv_penalty[((x)<<shift)-pred_x] + mv_penalty[((y)<<shift)-pred_y])*penalty_factor;\
        COPY3_IF_LT(dmin, d, best[0], x, best[1], y)\
    }\
}



#define ME_MAP_SIZE 64
#define ME_MAP_SHIFT 3
#define ME_MAP_MV_BITS 11
#define CHECK_CLIPPED_MV(ax,ay)\
{\
    const int Lx= ax;\
    const int Ly= ay;\
    const int Lx2= FFMAX(xmin, FFMIN(Lx, xmax));\
    const int Ly2= FFMAX(ymin, FFMIN(Ly, ymax));\
    CHECK_MV(Lx2, Ly2)\
}


#ifdef __GNUC__
#    define av_builtin_constant_p __builtin_constant_p
#    define av_printf_format(fmtpos, attrpos) __attribute__((__format__(__printf__, fmtpos, attrpos)))
#else
#    define av_builtin_constant_p(x) 0
#    define av_printf_format(fmtpos, attrpos)
#endif


#define FLAG_CHROMA 2

#define MAX_SAB_SIZE ME_MAP_SIZE


/* shape adaptive search stuff */
typedef struct Minima{
    int height;
    int x, y;
    int checked;
}Minima;


#define SAB_CHECK_MV(ax,ay)\
{\
    const unsigned key = ((ay)<<ME_MAP_MV_BITS) + (ax) + map_generation;\
    const int index= (((ay)<<ME_MAP_SHIFT) + (ax))&(ME_MAP_SIZE-1);\
    if(map[index]!=key){\
        d= cmp(s, ax, ay, 0, 0, size, h, ref_index, src_index, cmpf, chroma_cmpf, flags);\
        map[index]= key;\
        score_map[index]= d;\
        d += (mv_penalty[((ax)<<shift)-pred_x] + mv_penalty[((ay)<<shift)-pred_y])*penalty_factor;\
        if(d < minima[minima_count-1].height){\
            int j=0;\
            \
            while(d >= minima[j].height) j++;\
\
            memmove(&minima [j+1], &minima [j], (minima_count - j - 1)*sizeof(Minima));\
\
            minima[j].checked= 0;\
            minima[j].height= d;\
            minima[j].x= ax;\
            minima[j].y= ay;\
            \
            i=-1;\
            continue;\
        }\
    }\
}


#define CHECK_MV_DIR(x,y,new_dir)\
{\
    const unsigned key = ((y)<<ME_MAP_MV_BITS) + (x) + map_generation;\
    const int index= (((y)<<ME_MAP_SHIFT) + (x))&(ME_MAP_SIZE-1);\
    if(map[index]!=key){\
        d= cmp(s, x, y, 0, 0, size, h, ref_index, src_index, cmpf, chroma_cmpf, flags);\
        map[index]= key;\
        score_map[index]= d;\
        d += (mv_penalty[((x)<<shift)-pred_x] + mv_penalty[((y)<<shift)-pred_y])*penalty_factor;\
        if(d<dmin){\
            best[0]=x;\
            best[1]=y;\
            dmin=d;\
            next_dir= new_dir;\
        }\
    }\
}





#define MB_TYPE_8x8        0x0040
#define IS_8X8(a)        ((a)&MB_TYPE_8x8)

#define CHECK_QUARTER_MV(dx, dy, x, y)\
{\
    const int hx= 4*(x)+(dx);\
    const int hy= 4*(y)+(dy);\
    d= cmp_qpel(s, x, y, dx, dy, size, h, ref_index, src_index, cmpf, chroma_cmpf, flags);\
    d += (mv_penalty[hx - pred_x] + mv_penalty[hy - pred_y])*penalty_factor;\
    COPY3_IF_LT(dmin, d, bx, hx, by, hy)\
}

#define CHECK_HALF_MV(dx, dy, x, y)\
{\
    const int hx= 2*(x)+(dx);\
    const int hy= 2*(y)+(dy);\
    d= cmp_hpel(s, x, y, dx, dy, size, h, ref_index, src_index, cmp_sub, chroma_cmp_sub, flags);\
    d += (mv_penalty[hx - pred_x] + mv_penalty[hy - pred_y])*penalty_factor;\
    COPY3_IF_LT(dmin, d, bx, hx, by, hy)\
}

#define CHECK_QUARTER_MV(dx, dy, x, y)\
{\
    const int hx= 4*(x)+(dx);\
    const int hy= 4*(y)+(dy);\
    d= cmp_qpel(s, x, y, dx, dy, size, h, ref_index, src_index, cmpf, chroma_cmpf, flags);\
    d += (mv_penalty[hx - pred_x] + mv_penalty[hy - pred_y])*penalty_factor;\
    COPY3_IF_LT(dmin, d, bx, hx, by, hy)\
}


#define MB_TYPE_8x8        0x0040
#define MB_TYPE_16x16      0x0008
#define MB_TYPE_16x8       0x0010
#define MB_TYPE_P0L0       0x1000
#define MB_TYPE_P1L0       0x2000
#define MB_TYPE_P0L1       0x4000
#define MB_TYPE_P1L1       0x8000
#define MB_TYPE_DIRECT2    0x0100
#define MB_TYPE_L0         (MB_TYPE_P0L0 | MB_TYPE_P1L0)
#define MB_TYPE_L1         (MB_TYPE_P0L1 | MB_TYPE_P1L1)
#define MB_TYPE_L0L1       (MB_TYPE_L0   | MB_TYPE_L1)

#define USES_LIST(a, list) ((a) & ((MB_TYPE_P0L0|MB_TYPE_P1L0)<<(2*(list)))) ///< does this mb use listX, note does not work if subMBs

#define MB_TYPE_INTERLACED 0x0080
#define IS_INTERLACED(a) ((a)&MB_TYPE_INTERLACED)

/**
 * @ingroup lavc_encoding
 * motion estimation type.
 */
enum Motion_Est_ID {
    ME_ZERO = 1,    ///< no search, that is use 0,0 vector whenever one is needed
    ME_FULL,
    ME_LOG,
    ME_PHODS,
    ME_EPZS,        ///< enhanced predictive zonal search
    ME_X1,          ///< reserved for experiments
    ME_HEX,         ///< hexagon based search
    ME_UMH,         ///< uneven multi-hexagon search
    ME_ITER,        ///< iterative search
    ME_TESA,        ///< transformed exhaustive search algorithm
};


#define CONFIG_H261_DECODER 1

#   define FASTDIV(a,b) ((uint32_t)((((uint64_t)a) * ff_inverse[b]) >> 32))

#define MOTION_MARKER 0x1F001
#define DC_MARKER     0x6B001


#define CONFIG_SMALL 0

#define CODEC_FLAG_GRAY            0x2000   ///< Only decode/encode grayscale.

#define FF_LAMBDA_SCALE (1<<FF_LAMBDA_SHIFT)

#define CONFIG_GRAY 0

#define CONFIG_WMV2_DECODER 0


typedef union {
    uint32_t u32;
    uint16_t u16[2];
    uint8_t  u8 [4];
    float    f32;
} av_alias av_alias32;

#define CODEC_FLAG2_NO_OUTPUT     0x00000004 ///< Skip bitstream encoding.

#define CHAR_BIT __CHAR_BIT__
#    define INT_BIT (CHAR_BIT * sizeof(int))


#define IS_3IV1 0

#define tab_size ((signed)FF_ARRAY_ELEMS(s->direct_scale_mv[0]))
#define tab_bias (tab_size/2)

#define MB_TYPE_INTRA4x4   0x0001
#define MB_TYPE_INTRA MB_TYPE_INTRA4x4 //default mb_type if there is just one type



//-------------------------CPU最適化?うまくいかない

typedef struct GetByteContext {
    const uint8_t *buffer, *buffer_end, *buffer_start;
} GetByteContext;

typedef struct PutByteContext {
    uint8_t *buffer, *buffer_end, *buffer_start;
    int eof;
} PutByteContext;


#define DEF(type, name, bytes, read, write)                                  \
static inline type bytestream_get_ ## name(const uint8_t **b)        \
{                                                                              \
    (*b) += bytes;                                                             \
    return read(*b - bytes);                                                   \
}                                                                              \
static inline void bytestream_put_ ## name(uint8_t **b,              \
                                                     const type value)         \
{                                                                              \
    write(*b, value);                                                          \
    (*b) += bytes;                                                             \
}                                                                              \
static inline void bytestream2_put_ ## name ## u(PutByteContext *p,  \
                                                           const type value)   \
{                                                                              \
    bytestream_put_ ## name(&p->buffer, value);                                \
}                                                                              \
static inline void bytestream2_put_ ## name(PutByteContext *p,       \
                                                      const type value)        \
{                                                                              \
    if (!p->eof && (p->buffer_end - p->buffer >= bytes)) {                     \
        write(p->buffer, value);                                               \
        p->buffer += bytes;                                                    \
    } else                                                                     \
        p->eof = 1;                                                            \
}                                                                              \
static inline type bytestream2_get_ ## name ## u(GetByteContext *g)  \
{                                                                              \
    return bytestream_get_ ## name(&g->buffer);                                \
}                                                                              \
static inline type bytestream2_get_ ## name(GetByteContext *g)       \
{                                                                              \
    if (g->buffer_end - g->buffer < bytes)                                     \
        return 0;                                                              \
    return bytestream2_get_ ## name ## u(g);                                   \
}                                                                              \
static inline type bytestream2_peek_ ## name(GetByteContext *g)      \
{                                                                              \
    if (g->buffer_end - g->buffer < bytes)                                     \
        return 0;                                                              \
    return read(g->buffer);                                                    \
}


//#define AV_RB16 AV_RB16
//static inline uint16_t AV_RB16(const void *p)
//{
//    uint16_t v;
//    __asm__ ("ld.ub    %0,   %2  \n\t"
//             "ldins.b  %0:l, %1  \n\t"
//             : "=&r"(v)
//             : "RKs12"(*(const uint8_t*)p), "m"(*((const uint8_t*)p+1)));
//    return v;
//}

//#define AV_RB16 AV_RB16
//static inline uint16_t AV_RB16(const void *p)
//{
//    uint16_t v;
//    __asm__ ("loadacc,   (%1+) \n\t"
//             "rol8             \n\t"
//             "storeacc,  %0    \n\t"
//             "loadacc,   (%1+) \n\t"
//             "add,       %0    \n\t"
//             : "=r"(v), "+a"(p));
//    return v;
//}




DEF(uint64_t,     le64, 8, AV_RL64, AV_WL64)
DEF(unsigned int, le32, 4, AV_RL32, AV_WL32)
DEF(unsigned int, le24, 3, AV_RL24, AV_WL24)
DEF(unsigned int, le16, 2, AV_RL16, AV_WL16)
DEF(uint64_t,     be64, 8, AV_RB64, AV_WB64)
DEF(unsigned int, be32, 4, AV_RB32, AV_WB32)
DEF(unsigned int, be24, 3, AV_RB24, AV_WB24)
DEF(unsigned int, be16, 2, AV_RB16, AV_WB16)
DEF(unsigned int, byte, 1, AV_RB8 , AV_WB8)


int avcodec_encode_audio2(AVCodecContext *avctx,
                                              AVPacket *avpkt,
                                              const AVFrame *frame,
                                              int *got_packet_ptr);

//--------------------------------------------------

av_cold int avcodec_close(AVCodecContext *avctx);

int avcodec_set_context_defaults3(AVCodecContext *s, const AVCodec *codec);
