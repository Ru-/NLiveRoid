

#ifndef UTIL_H_
#include "util.h"
#define UTIL_H_
#endif

#ifndef CODEC_BASE_H_
#include "codec_base.h"
#define CODEC_BASE_H_
#endif

#include "get_params.h"

#include <pthread.h>
#include <stdarg.h>
#include <string.h>
#include <stdint.h>
#include <stdlib.h>
#include <math.h>
#include <android/log.h>
#define LOG_TAG "NLiveRoid"
#define LOGD(...)  __android_log_print(3, LOG_TAG, __VA_ARGS__)


uint8_t ff_sqrt_tab[256];
uint32_t ff_inverse[257];
const uint8_t ff_h263_intra_MCBPC_code[9] = { 1, 1, 2, 3, 1, 1, 2, 3, 1 };
const uint8_t ff_h263_intra_MCBPC_bits[9] = { 1, 3, 3, 3, 4, 6, 6, 6, 9 };
const uint8_t ff_h263_inter_MCBPC_code[28] = {
    1, 3, 2, 5,
    3, 4, 3, 3,
    3, 7, 6, 5,
    4, 4, 3, 2,
    2, 5, 4, 5,
    1, 0, 0, 0, /* Stuffing */
    2, 12, 14, 15,
};
const uint8_t ff_h263_inter_MCBPC_bits[28] = {
    1, 4, 4, 6, /* inter  */
    5, 8, 8, 7, /* intra  */
    3, 7, 7, 9, /* interQ */
    6, 9, 9, 9, /* intraQ */
    3, 7, 7, 8, /* inter4 */
    9, 0, 0, 0, /* Stuffing */
    11, 13, 13, 13,/* inter4Q*/
};

const uint8_t ff_h263_cbpy_tab[16][2] =
{
  {3,4}, {5,5}, {4,5}, {9,4}, {3,5}, {7,4}, {2,6}, {11,4},
  {2,5}, {3,6}, {5,4}, {10,4}, {4,4}, {8,4}, {6,4}, {3,2}
};

#define DUP_DATA(dst, src, size, padding)                               \
    do {                                                                \
        void *data;                                                     \
        if (padding) {                                                  \
            if ((unsigned)(size) >                                      \
                (unsigned)(size) + FF_INPUT_BUFFER_PADDING_SIZE)        \
                goto failed_alloc;                                      \
            data = av_malloc(size + FF_INPUT_BUFFER_PADDING_SIZE);      \
        } else {                                                        \
            data = av_malloc(size);                                     \
        }                                                               \
        if (!data)                                                      \
            goto failed_alloc;                                          \
        memcpy(data, src, size);                                        \
        if (padding)                                                    \
            memset((uint8_t *)data + size, 0,                           \
                   FF_INPUT_BUFFER_PADDING_SIZE);                       \
        dst = data;                                                     \
    } while (0)



static int (*ff_lockmgr_cb)(void **mutex, enum AVLockOp op);
static void *codec_mutex;
static int volatile entangled_thread_counter = 0;
volatile int ff_avcodec_locked;


static const SampleFmtInfo sample_fmt_info[AV_SAMPLE_FMT_NB] = {
    [AV_SAMPLE_FMT_U8]   = { .name =   "u8", .bits =  8, .planar = 0, .altform = AV_SAMPLE_FMT_U8P  },
    [AV_SAMPLE_FMT_S16]  = { .name =  "s16", .bits = 16, .planar = 0, .altform = AV_SAMPLE_FMT_S16P },
    [AV_SAMPLE_FMT_S32]  = { .name =  "s32", .bits = 32, .planar = 0, .altform = AV_SAMPLE_FMT_S32P },
    [AV_SAMPLE_FMT_FLT]  = { .name =  "flt", .bits = 32, .planar = 0, .altform = AV_SAMPLE_FMT_FLTP },
    [AV_SAMPLE_FMT_DBL]  = { .name =  "dbl", .bits = 64, .planar = 0, .altform = AV_SAMPLE_FMT_DBLP },
    [AV_SAMPLE_FMT_U8P]  = { .name =  "u8p", .bits =  8, .planar = 1, .altform = AV_SAMPLE_FMT_U8   },
    [AV_SAMPLE_FMT_S16P] = { .name = "s16p", .bits = 16, .planar = 1, .altform = AV_SAMPLE_FMT_S16  },
    [AV_SAMPLE_FMT_S32P] = { .name = "s32p", .bits = 32, .planar = 1, .altform = AV_SAMPLE_FMT_S32  },
    [AV_SAMPLE_FMT_FLTP] = { .name = "fltp", .bits = 32, .planar = 1, .altform = AV_SAMPLE_FMT_FLT  },
    [AV_SAMPLE_FMT_DBLP] = { .name = "dblp", .bits = 64, .planar = 1, .altform = AV_SAMPLE_FMT_DBL  },
};


static const struct {
    const char *name;
    int         nb_channels;
    uint64_t     layout;
} channel_layout_map[] = {
    { "mono",        1,  AV_CH_LAYOUT_MONO },
    { "stereo",      2,  AV_CH_LAYOUT_STEREO },
    { "2.1",         3,  AV_CH_LAYOUT_2POINT1 },
    { "3.0",         3,  AV_CH_LAYOUT_SURROUND },
    { "3.0(back)",   3,  AV_CH_LAYOUT_2_1 },
    { "4.0",         4,  AV_CH_LAYOUT_4POINT0 },
    { "quad",        4,  AV_CH_LAYOUT_QUAD },
    { "quad(side)",  4,  AV_CH_LAYOUT_2_2 },
    { "3.1",         4,  AV_CH_LAYOUT_3POINT1 },
    { "5.0",         5,  AV_CH_LAYOUT_5POINT0_BACK },
    { "5.0(side)",   5,  AV_CH_LAYOUT_5POINT0 },
    { "4.1",         5,  AV_CH_LAYOUT_4POINT1 },
    { "5.1",         6,  AV_CH_LAYOUT_5POINT1_BACK },
    { "5.1(side)",   6,  AV_CH_LAYOUT_5POINT1 },
    { "6.0",         6,  AV_CH_LAYOUT_6POINT0 },
    { "6.0(front)",  6,  AV_CH_LAYOUT_6POINT0_FRONT },
    { "hexagonal",   6,  AV_CH_LAYOUT_HEXAGONAL },
    { "6.1",         7,  AV_CH_LAYOUT_6POINT1 },
    { "6.1",         7,  AV_CH_LAYOUT_6POINT1_BACK },
    { "6.1(front)",  7,  AV_CH_LAYOUT_6POINT1_FRONT },
    { "7.0",         7,  AV_CH_LAYOUT_7POINT0 },
    { "7.0(front)",  7,  AV_CH_LAYOUT_7POINT0_FRONT },
    { "7.1",         8,  AV_CH_LAYOUT_7POINT1 },
    { "7.1(wide)",   8,  AV_CH_LAYOUT_7POINT1_WIDE_BACK },
    { "7.1(wide-side)",   8,  AV_CH_LAYOUT_7POINT1_WIDE },
    { "octagonal",   8,  AV_CH_LAYOUT_OCTAGONAL },
    { "downmix",     2,  AV_CH_LAYOUT_STEREO_DOWNMIX, },
};

struct channel_name {
    const char *name;
    const char *description;
};

static const struct channel_name channel_names[] = {
     [0] = { "FL",        "front left"            },
     [1] = { "FR",        "front right"           },
     [2] = { "FC",        "front center"          },
     [3] = { "LFE",       "low frequency"         },
     [4] = { "BL",        "back left"             },
     [5] = { "BR",        "back right"            },
     [6] = { "FLC",       "front left-of-center"  },
     [7] = { "FRC",       "front right-of-center" },
     [8] = { "BC",        "back center"           },
     [9] = { "SL",        "side left"             },
    [10] = { "SR",        "side right"            },
    [11] = { "TC",        "top center"            },
    [12] = { "TFL",       "top front left"        },
    [13] = { "TFC",       "top front center"      },
    [14] = { "TFR",       "top front right"       },
    [15] = { "TBL",       "top back left"         },
    [16] = { "TBC",       "top back center"       },
    [17] = { "TBR",       "top back right"        },
    [29] = { "DL",        "downmix left"          },
    [30] = { "DR",        "downmix right"         },
    [31] = { "WL",        "wide left"             },
    [32] = { "WR",        "wide right"            },
    [33] = { "SDL",       "surround direct left"  },
    [34] = { "SDR",       "surround direct right" },
    [35] = { "LFE2",      "low frequency 2"       },
};



static const uint8_t mpeg2_dc_scale_table1[128] = {
//  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
};

static const uint8_t mpeg2_dc_scale_table2[128] = {
//  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
};

static const uint8_t mpeg2_dc_scale_table3[128] = {
//  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
};

const uint16_t ff_mba_max[6]={
     47,  98, 395,1583,6335,9215
};
const uint8_t ff_mba_length[7]={
      6,   7,   9,  11,  13,  14,  14
};

const uint8_t *const ff_mpeg2_dc_scale_table[4] = {
    ff_mpeg1_dc_scale_table,
    mpeg2_dc_scale_table1,
    mpeg2_dc_scale_table2,
    mpeg2_dc_scale_table3,
};


static uint8_t  uni_mpeg4_intra_rl_len [64*64*2*2];
static uint8_t  uni_mpeg4_inter_rl_len [64*64*2*2];
static uint32_t uni_mpeg4_intra_rl_bits[64*64*2*2];
static uint32_t uni_mpeg4_inter_rl_bits[64*64*2*2];

static uint8_t uni_DCtab_lum_len[512];
static uint8_t uni_DCtab_chrom_len[512];
static uint16_t uni_DCtab_lum_bits[512];
static uint16_t uni_DCtab_chrom_bits[512];


static uint8_t rl_length[NB_RL_TABLES][MAX_LEVEL+1][MAX_RUN+1][2];

static const uint8_t wrong_run[102] = {
 1,  2,  3,  5,  4, 10,  9,  8,
11, 15, 17, 16, 23, 22, 21, 20,
19, 18, 25, 24, 27, 26, 11,  7,
 6,  1,  2, 13,  2,  2,  2,  2,
 6, 12,  3,  9,  1,  3,  4,  3,
 7,  4,  1,  1,  5,  5, 14,  6,
 1,  7,  1,  8,  1,  1,  1,  1,
10,  1,  1,  5,  9, 17, 25, 24,
29, 33, 32, 41,  2, 23, 28, 31,
 3, 22, 30,  4, 27, 40,  8, 26,
 6, 39,  7, 38, 16, 37, 15, 10,
11, 12, 13, 14,  1, 21, 20, 18,
19,  2,  1, 34, 35, 36
};

int av_image_fill_pointers(uint8_t *data[4], enum AVPixelFormat pix_fmt, int height,
                           uint8_t *ptr, const int linesizes[4])
{
//	LOGD("av_image_fill_pointers Called\n");
    int i, total_size, size[4] = { 0 }, has_plane[4] = { 0 };

    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);
    memset(data     , 0, sizeof(data[0])*4);
    data[0] = ptr;

//    if (!desc || desc->flags & PIX_FMT_HWACCEL)
//        return -1;

//    if (linesizes[0] > (INT_MAX - 1024) / height)
//        return -1;

    size[0] = linesizes[0] * height;


//    if (desc->flags & PIX_FMT_PAL ||
//        (desc->flags & PIX_FMT_PSEUDOPAL)) {
//        size[0] = (size[0] + 3) & ~3;
//        data[1] = ptr + size[0]; /* palette is stored here as 256 32 bits words */
//        return size[0] + 256 * 4;
//    }


    for (i = 0; i < 4; i++)
        has_plane[desc->comp[i].plane] = 1;


    total_size = size[0];
    for (i = 1; i < 4 && has_plane[i]; i++) {
        int h, s = (i == 1 || i == 2) ? desc->log2_chroma_h : 0;
        data[i] = data[i-1] + size[i-1];
        h = (height + (1 << s) - 1) >> s;
        if (linesizes[i] > INT_MAX / h)
            return -1;
        size[i] = h * linesizes[i];
        if (total_size > INT_MAX - size[i])
            return -1;
        total_size += size[i];
    }

    return total_size;
}


int avpriv_set_systematic_pal2(uint32_t pal[256], enum AVPixelFormat pix_fmt)
{
    LOGD("avpriv_set_systematic_pal2 Called\n");
    int i;

    for (i = 0; i < 256; i++) {
        int r, g, b;

        switch (pix_fmt) {
        case AV_PIX_FMT_RGB8:
            r = (i>>5    )*36;
            g = ((i>>2)&7)*36;
            b = (i&3     )*85;
            break;
        case AV_PIX_FMT_BGR8:
            b = (i>>6    )*85;
            g = ((i>>3)&7)*36;
            r = (i&7     )*36;
            break;
        case AV_PIX_FMT_RGB4_BYTE:
            r = (i>>3    )*255;
            g = ((i>>1)&3)*85;
            b = (i&1     )*255;
            break;
        case AV_PIX_FMT_BGR4_BYTE:
            b = (i>>3    )*255;
            g = ((i>>1)&3)*85;
            r = (i&1     )*255;
            break;
        case AV_PIX_FMT_GRAY8:
            r = b = g = i;
            break;
        default:
            return -1;
        }
        pal[i] = b + (g<<8) + (r<<16) + (0xFFU<<24);
    }

    return 0;
}




int av_image_check_size(unsigned int w, unsigned int h, int log_offset, void *log_ctx)
{
    if ((int)w>0 && (int)h>0 && (w+128)*(uint64_t)(h+128) < INT_MAX/8)
        return 0;

    LOGD( "Picture size %ux%u is invalid\n", w, h);
    return -1;
}
#define AV_PIX_FMT_FLAG_HWACCEL      (1 << 3)
int av_image_fill_linesizes(int linesizes[4], enum AVPixelFormat pix_fmt, int width)
{
    int i, ret;
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);

    int max_step     [4];       /* max pixel step for each plane */
    int max_step_comp[4];       /* the component for each plane which has the max pixel step */

//    memset(linesizes, 0, 4*sizeof(linesizes[0]));
//    if (!desc || (desc->flags & AV_PIX_FMT_FLAG_HWACCEL))
//        return -1;

    av_image_fill_max_pixsteps(max_step, max_step_comp, desc);
    for (i = 0; i < 4; i++) {
        if ((ret = image_get_linesize(width, i, max_step[i], max_step_comp[i], desc)) < 0)
            return ret;
        linesizes[i] = ret;
    }

    return 0;
}
//av_image_fill_arrays
int avpicture_fill(uint8_t *dst_data[4], int dst_linesize[4],
                         const uint8_t *src,
                         enum AVPixelFormat pix_fmt, int width, int height, int align)
{
//	LOGD("av_image_fill_arrays Called\n");
	    int ret;

	    if ((ret = av_image_fill_linesizes(dst_linesize, pix_fmt, width)) < 0)
	        return ret;

	    if ((ret = av_image_fill_pointers(dst_data, pix_fmt, width, NULL, dst_linesize)) < 0){
	    		return ret;
	    }
	    return av_image_fill_pointers(dst_data, pix_fmt, height, (uint8_t *)src, dst_linesize);
	}

void avcodec_align_dimensions2(AVCodecContext *s, int *width, int *height,
                               int linesize_align[AV_NUM_DATA_POINTERS])
{
    int i;
    int w_align = 1;
    int h_align = 1;

    switch (s->pix_fmt) {
    case AV_PIX_FMT_YUV420P:
    case AV_PIX_FMT_YUYV422:
    case AV_PIX_FMT_UYVY422:
    case AV_PIX_FMT_YUV422P:
    case AV_PIX_FMT_YUV440P:
    case AV_PIX_FMT_YUV444P:
    case AV_PIX_FMT_GBRP:
    case AV_PIX_FMT_GRAY8:
    case AV_PIX_FMT_GRAY16BE:
    case AV_PIX_FMT_GRAY16LE:
    case AV_PIX_FMT_YUVJ420P:
    case AV_PIX_FMT_YUVJ422P:
    case AV_PIX_FMT_YUVJ440P:
    case AV_PIX_FMT_YUVJ444P:
    case AV_PIX_FMT_YUVA420P:
    case AV_PIX_FMT_YUVA422P:
    case AV_PIX_FMT_YUVA444P:
    case AV_PIX_FMT_YUV420P9LE:
    case AV_PIX_FMT_YUV420P9BE:
    case AV_PIX_FMT_YUV420P10LE:
    case AV_PIX_FMT_YUV420P10BE:
    case AV_PIX_FMT_YUV420P12LE:
    case AV_PIX_FMT_YUV420P12BE:
    case AV_PIX_FMT_YUV420P14LE:
    case AV_PIX_FMT_YUV420P14BE:
    case AV_PIX_FMT_YUV422P9LE:
    case AV_PIX_FMT_YUV422P9BE:
    case AV_PIX_FMT_YUV422P10LE:
    case AV_PIX_FMT_YUV422P10BE:
    case AV_PIX_FMT_YUV422P12LE:
    case AV_PIX_FMT_YUV422P12BE:
    case AV_PIX_FMT_YUV422P14LE:
    case AV_PIX_FMT_YUV422P14BE:
    case AV_PIX_FMT_YUV444P9LE:
    case AV_PIX_FMT_YUV444P9BE:
    case AV_PIX_FMT_YUV444P10LE:
    case AV_PIX_FMT_YUV444P10BE:
    case AV_PIX_FMT_YUV444P12LE:
    case AV_PIX_FMT_YUV444P12BE:
    case AV_PIX_FMT_YUV444P14LE:
    case AV_PIX_FMT_YUV444P14BE:
    case AV_PIX_FMT_GBRP9LE:
    case AV_PIX_FMT_GBRP9BE:
    case AV_PIX_FMT_GBRP10LE:
    case AV_PIX_FMT_GBRP10BE:
    case AV_PIX_FMT_GBRP12LE:
    case AV_PIX_FMT_GBRP12BE:
    case AV_PIX_FMT_GBRP14LE:
    case AV_PIX_FMT_GBRP14BE:
        w_align = 16; //FIXME assume 16 pixel per macroblock
        h_align = 16 * 2; // interlaced needs 2 macroblocks height
        break;
    case AV_PIX_FMT_YUV411P:
    case AV_PIX_FMT_UYYVYY411:
        w_align = 32;
        h_align = 8;
        break;
    case AV_PIX_FMT_YUV410P:
        if (s->codec_id == AV_CODEC_ID_SVQ1) {
            w_align = 64;
            h_align = 64;
        }
        break;
    case AV_PIX_FMT_PAL8:
    case AV_PIX_FMT_BGR8:
    case AV_PIX_FMT_RGB8:
        if (s->codec_id == AV_CODEC_ID_SMC ||
            s->codec_id == AV_CODEC_ID_CINEPAK) {
            w_align = 4;
            h_align = 4;
        }
        break;
    case AV_PIX_FMT_BGR24:
        if ((s->codec_id == AV_CODEC_ID_MSZH) ||
            (s->codec_id == AV_CODEC_ID_ZLIB)) {
            w_align = 4;
            h_align = 4;
        }
        break;
    case AV_PIX_FMT_RGB24:
        if (s->codec_id == AV_CODEC_ID_CINEPAK) {
            w_align = 4;
            h_align = 4;
        }
        break;
    default:
        w_align = 1;
        h_align = 1;
        break;
    }

    if (s->codec_id == AV_CODEC_ID_IFF_ILBM || s->codec_id == AV_CODEC_ID_IFF_BYTERUN1) {
        w_align = FFMAX(w_align, 8);
    }

    *width  = FFALIGN(*width, w_align);
    *height = FFALIGN(*height, h_align);
    if (s->codec_id == AV_CODEC_ID_H264 || s->lowres)
        // some of the optimized chroma MC reads one line too much
        // which is also done in mpeg decoders with lowres > 0
        *height += 2;

    for (i = 0; i < 4; i++)
        linesize_align[i] = STRIDE_ALIGN;
}



int av_pix_fmt_get_chroma_sub_sample(enum AVPixelFormat pix_fmt,
                                     int *h_shift, int *v_shift)
{
//    LOGD("av_pix_fmt_get_chroma_sub_sample Called\n");
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);
    if (!desc)
        return -1;
    *h_shift = desc->log2_chroma_w;
    *v_shift = desc->log2_chroma_h;

    return 0;
}


int av_get_bytes_per_sample(enum AVSampleFormat sample_fmt)
{
     return sample_fmt < 0 || sample_fmt >= AV_SAMPLE_FMT_NB ?
        0 : sample_fmt_info[sample_fmt].bits >> 3;
}


int av_sample_fmt_is_planar(enum AVSampleFormat sample_fmt)
{
     if (sample_fmt < 0 || sample_fmt >= AV_SAMPLE_FMT_NB)
         return 0;
     return sample_fmt_info[sample_fmt].planar;
}



int av_samples_fill_arrays(uint8_t **audio_data, int *linesize,
                           const uint8_t *buf, int nb_channels, int nb_samples,
                           enum AVSampleFormat sample_fmt, int align)
{	//audio_dataはframe->extended_data,line_ssizeはここではまだ0,nb_channels1,nb_samples1152,sample_fmt6
    LOGD("av_samples_get_buffer_size av_samples_fill_arrays Called\n");
    LOGD("av_samples_get_buffer_size av_samples_fill_arrays linesize:%d\n",*linesize);
    LOGD("av_samples_get_buffer_size av_samples_fill_arrays nb_channels1:%d\n",nb_channels);
    int ch, planar, buf_size, line_size;

    planar   = av_sample_fmt_is_planar(sample_fmt);
    buf_size = av_samples_get_buffer_size(&line_size, nb_channels, nb_samples,
                                          sample_fmt, align);
    LOGD("av_samples_get_buffer_size av_samples_fill_arrays buf_size:%d\n",buf_size);
    if (buf_size < 0)
        return buf_size;

    audio_data[0] = (uint8_t *)buf;//frame->extended_dataに入れている
    for (ch = 1; planar && ch < nb_channels; ch++)
        audio_data[ch] = audio_data[ch-1] + line_size;

    if (linesize)
        *linesize = line_size;

#if FF_API_SAMPLES_UTILS_RETURN_ZERO
    return 0;
#else
    return buf_size;
#endif
}


int avcodec_fill_audio_frame(AVFrame *frame, int nb_channels,
                             enum AVSampleFormat sample_fmt, const uint8_t *buf,
                             int buf_size, int align)
{
    int ch, planar, needed_size, ret = 0;

    needed_size = av_samples_get_buffer_size(NULL, nb_channels,
                                             frame->nb_samples, sample_fmt,
                                             align);
    if (buf_size < needed_size)
        return -1;

    planar = av_sample_fmt_is_planar(sample_fmt);
    if (planar && nb_channels > AV_NUM_DATA_POINTERS) {
        if (!(frame->extended_data = av_mallocz(nb_channels *
                                                sizeof(*frame->extended_data))))
            return -1;
    } else {
        frame->extended_data = frame->data;
    }

    if ((ret = av_samples_fill_arrays(frame->extended_data, &frame->linesize[0],
                                      (uint8_t *)(intptr_t)buf, nb_channels, frame->nb_samples,
                                      sample_fmt, align)) < 0) {
        if (frame->extended_data != frame->data){
            LOGD("avcodec_fill_audio_frame frame->extended_data != frame->data\n");
            av_freep(&frame->extended_data);
        return ret;
        }
    }
    if (frame->extended_data != frame->data) {//実行されていない
        for (ch = 0; ch < AV_NUM_DATA_POINTERS; ch++)
            frame->data[ch] = frame->extended_data[ch];
    }

    return ret;
}


const AVRational ff_h263_pixel_aspect[16]={
 {0, 1},
 {1, 1},
 {12, 11},
 {10, 11},
 {16, 11},
 {40, 33},
 {0, 1},
 {0, 1},
 {0, 1},
 {0, 1},
 {0, 1},
 {0, 1},
 {0, 1},
 {0, 1},
 {0, 1},
 {0, 1},
};


const uint8_t ff_aic_dc_scale_table[32]={
//  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
    0, 2, 4, 6, 8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58,60,62
};




int ff_mpv_frame_size_alloc(MpegEncContext *s, int linesize)
{
    int alloc_size = FFALIGN(FFABS(linesize) + 64, 32);

    // edge emu needs blocksize + filter length - 1
    // (= 17x17 for  halfpel / 21x21 for  h264)
    // VC1 computes luma and chroma simultaneously and needs 19X19 + 9x9
    // at uvlinesize. It supports only YUV420 so 24x24 is enough
    // linesize * interlaced * MBsize
    FF_ALLOCZ_OR_GOTO(s->avctx, s->edge_emu_buffer, alloc_size * 4 * 24,
                      fail);

    FF_ALLOCZ_OR_GOTO(s->avctx, s->me.scratchpad, alloc_size * 4 * 16 * 2,
                      fail)
    s->me.temp         = s->me.scratchpad;
    s->rd_scratchpad   = s->me.scratchpad;
    s->b_scratchpad    = s->me.scratchpad;
    s->obmc_scratchpad = s->me.scratchpad + 16;

    return 0;
fail:
    av_freep(&s->edge_emu_buffer);
    return -1;
}



/**
 * Allocate a frame buffer
 */
static int alloc_frame_buffer(MpegEncContext *s, Picture *pic)
{
//    LOGD("alloc_frame_buffer linesize:%d %d\n",pic->f.linesize[0],pic->f.linesize[1],pic->f.linesize[2],pic->f.linesize[3]);
    int r, ret;

    if (s->avctx->hwaccel) {
//        assert(!pic->f.hwaccel_picture_private);
        if (s->avctx->hwaccel->priv_data_size) {
            pic->f.hwaccel_picture_private = av_mallocz(s->avctx->hwaccel->priv_data_size);
            if (!pic->f.hwaccel_picture_private) {
                LOGD( "alloc_frame_buffer() failed (hwaccel private data allocation)\n");
                return -1;
            }
        }
    }

    if (s->codec_id != AV_CODEC_ID_WMV3IMAGE &&
        s->codec_id != AV_CODEC_ID_VC1IMAGE  &&
        s->codec_id != AV_CODEC_ID_MSS2)
        r = ff_thread_get_buffer(s->avctx, &pic->f);
    else
        r = avcodec_default_get_buffer(s->avctx, &pic->f);

    if (r < 0 || !pic->f.type || !pic->f.data[0]) {
    	LOGD("get_buffer() failed (%d %d %p)\n",
               r, pic->f.type, pic->f.data[0]);
        av_freep(&pic->f.hwaccel_picture_private);
        return -1;
    }

    if (s->linesize && (s->linesize   != pic->f.linesize[0] ||
                        s->uvlinesize != pic->f.linesize[1])) {
    	LOGD("get_buffer() failed (stride changed)\n");
        free_frame_buffer(s, pic);
        return -1;
    }
    if (pic->f.linesize[1] != pic->f.linesize[2]) {
    	LOGD("get_buffer() failed (uv stride mismatch)\n");
        free_frame_buffer(s, pic);
        return -1;
    }

    if (!s->edge_emu_buffer &&
        (ret = ff_mpv_frame_size_alloc(s, pic->f.linesize[0])) < 0) {
    	LOGD( "get_buffer() failed to allocate context scratch buffers.\n");
        free_frame_buffer(s, pic);
        return ret;
    }

    return 0;
}

/**
 * Allocate a Picture.
 * The pixels are allocated/set by calling get_buffer() if shared = 0
 */
int ff_alloc_picture(MpegEncContext *s, Picture *pic, int shared)
{
//	LOGD("ff_alloc_picture Called\n");
    const int big_mb_num = s->mb_stride * (s->mb_height + 1) + 1;

    // the + 1 is needed so memset(,,stride*height) does not sig11

    const int mb_array_size = s->mb_stride * s->mb_height;
    const int b8_array_size = s->b8_stride * s->mb_height * 2;
    const int b4_array_size = s->b4_stride * s->mb_height * 4;
    int i;
    int r = -1;

//	LOGD("ff_alloc_picture shared:%d\n",shared);
    if (shared) {
//        assert(pic->f.data[0]);
//        assert(pic->f.type == 0 || pic->f.type == FF_BUFFER_TYPE_SHARED);
        pic->f.type = FF_BUFFER_TYPE_SHARED;
    } else {
//        assert(!pic->f.data[0]);

        if (alloc_frame_buffer(s, pic) < 0)
            return -1;


        s->linesize   = pic->f.linesize[0];
        s->uvlinesize = pic->f.linesize[1];
    }

    if (pic->f.qscale_table == NULL) {
        if (s->encoding) {

            FF_ALLOCZ_OR_GOTO(s->avctx, pic->mb_var,
                              mb_array_size * sizeof(int16_t), fail)
            FF_ALLOCZ_OR_GOTO(s->avctx, pic->mc_mb_var,
                              mb_array_size * sizeof(int16_t), fail)
            FF_ALLOCZ_OR_GOTO(s->avctx, pic->mb_mean,
                              mb_array_size * sizeof(int8_t ), fail)
        }

        FF_ALLOCZ_OR_GOTO(s->avctx, pic->f.mbskip_table,
                          mb_array_size * sizeof(uint8_t) + 2, fail)// the + 2 is for the slice end check
        FF_ALLOCZ_OR_GOTO(s->avctx, pic->qscale_table_base,
                          (big_mb_num + s->mb_stride) * sizeof(uint8_t),
                          fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, pic->mb_type_base,
                          (big_mb_num + s->mb_stride) * sizeof(uint32_t),
                          fail)
        pic->f.mb_type = pic->mb_type_base + 2 * s->mb_stride + 1;
        pic->f.qscale_table = pic->qscale_table_base + 2 * s->mb_stride + 1;
        if (s->out_format == FMT_H264) {
            for (i = 0; i < 2; i++) {
                FF_ALLOCZ_OR_GOTO(s->avctx, pic->motion_val_base[i],
                                  2 * (b4_array_size + 4) * sizeof(int16_t),
                                  fail)
                pic->f.motion_val[i] = pic->motion_val_base[i] + 4;
                FF_ALLOCZ_OR_GOTO(s->avctx, pic->f.ref_index[i],
                                  4 * mb_array_size * sizeof(uint8_t), fail)
            }
            pic->f.motion_subsample_log2 = 2;
        } else if (s->out_format == FMT_H263 || s->encoding ||
                   (s->avctx->debug & FF_DEBUG_MV) || s->avctx->debug_mv) {
            for (i = 0; i < 2; i++) {
                FF_ALLOCZ_OR_GOTO(s->avctx, pic->motion_val_base[i],
                                  2 * (b8_array_size + 4) * sizeof(int16_t),
                                  fail)
                pic->f.motion_val[i] = pic->motion_val_base[i] + 4;
                FF_ALLOCZ_OR_GOTO(s->avctx, pic->f.ref_index[i],
                                  4 * mb_array_size * sizeof(uint8_t), fail)
            }
            pic->f.motion_subsample_log2 = 3;
        }
        if (s->avctx->debug&FF_DEBUG_DCT_COEFF) {
            FF_ALLOCZ_OR_GOTO(s->avctx, pic->f.dct_coeff,
                              64 * mb_array_size * sizeof(int16_t) * 6, fail)
        }
    	LOGD("ff_alloc_picture sizeof(AVPanScan):%d \n",sizeof(AVPanScan));
        pic->f.qstride = s->mb_stride;
        FF_ALLOCZ_OR_GOTO(s->avctx, pic->f.pan_scan,
                          1 * sizeof(AVPanScan), fail)
    }

    pic->owner2 = s;
    LOGD("ff_alloc_picture END");
    return 0;
fail: // for  the FF_ALLOCZ_OR_GOTO macro
    if (r >= 0)
        free_frame_buffer(s, pic);
    return -1;
}


static inline int pic_is_unused(MpegEncContext *s, Picture *pic)
{
//	LOGD("pic_is_unused Called\n");
    if (   (s->avctx->active_thread_type & FF_THREAD_FRAME)
        && pic->f.qscale_table //check if the frame has anything allocated
        && pic->period_since_free < s->avctx->thread_count)
        return 0;
    if (pic->f.data[0] == NULL)
        return 1;
    if (pic->needs_realloc && !(pic->f.reference & DELAYED_PIC_REF))
        if (!pic->owner2 || pic->owner2 == s)
            return 1;
    return 0;
}

static int find_unused_picture(MpegEncContext *s, int shared)
{
    int i;
//実装は違うがリターンは合ってた
//    LOGD("find_unused_picture Called\n");
    if (shared) {
        for (i = s->picture_range_start; i < s->picture_range_end; i++) {
            if (s->picture[i].f.data[0] == NULL && s->picture[i].f.type == 0){
//                LOGD("find_unused_picture RETURNED:%d\n",i);
                return i;
            }
        }
    } else {
//        LOGD("find_unused_picture range_s:%d range_e:%d\n",s->picture_range_start,s->picture_range_end);
        for (i = s->picture_range_start; i < s->picture_range_end; i++) {
            if (pic_is_unused(s, &s->picture[i]) && s->picture[i].f.type != 0){
//                LOGD("find_unused_picture RETURNED:%d\n",i);
                return i; // FIXME
            }
        }
        for (i = s->picture_range_start; i < s->picture_range_end; i++) {
            if (pic_is_unused(s, &s->picture[i])){
//                LOGD("find_unused_picture RETURNED:%d\n",i);
                return i;
            }
        }
    }

    //s->picturesに、MAX_PICTURE_COUNTを超えたPictureが格納されようとすると、ここでエラーする
    LOGD("Internal error, picture buffer overflow\n");
    /* We could return -1, but the codec would crash trying to draw into a
     * non-existing frame anyway. This is safer than waiting for a random crash.
     * Also the return of this is never useful, an encoder must only allocate
     * as much as allowed in the specification. This has no relationship to how
     * much libavcodec could allocate (and MAX_PICTURE_COUNT is always large
     * enough for such valid streams).
     * Plus, a decoder has to check stream validity and remove frames if too
     * many reference frames are around. Waiting for "OOM" is not correct at
     * all. Similarly, missing reference frames have to be replaced by
     * interpolated/MC frames, anything else is a bug in the codec ...
     */
    abort();
    return -1;
}


int ff_find_unused_picture(MpegEncContext *s, int shared)
{
    int ret = find_unused_picture(s, shared);

    if (ret >= 0 && ret < s->picture_range_end) {
        if (s->picture[ret].needs_realloc) {
            s->picture[ret].needs_realloc = 0;
            free_picture(s, &s->picture[ret]);
            avcodec_get_frame_defaults(&s->picture[ret].f);
        }
    }
    return ret;
}


static void copy_picture_attributes(MpegEncContext *s, AVFrame *dst,
                                    const AVFrame *src)
{
    int i;

    dst->pict_type              = src->pict_type;
    dst->quality                = src->quality;
    dst->coded_picture_number   = src->coded_picture_number;
    dst->display_picture_number = src->display_picture_number;
    //dst->reference              = src->reference;
    dst->pts                    = src->pts;
    dst->interlaced_frame       = src->interlaced_frame;
    dst->top_field_first        = src->top_field_first;
//    LOGD("dst key_frame:%d\n",dst->key_frame              );
//    LOGD("dst pict_type:%d\n",dst->pict_type              );
//    LOGD("dst sample_aspect_ratio:%d\n",dst->sample_aspect_ratio    );
//    LOGD("dst pts:%d\n",dst->pts                    );
//    LOGD("dst repeat_pict:%d\n",dst->repeat_pict            );
//    LOGD("dst interlaced_frame:%d\n",dst->interlaced_frame       );
//    LOGD("dst top_field_first:%d\n",dst->top_field_first        );
//    LOGD("dst palette_has_changed:%d\n",dst->palette_has_changed    );
//    LOGD("dst sample_rate:%d\n",dst->sample_rate            );
//    LOGD("dst opaque:%d\n",dst->opaque                 );
//    LOGD("dst pkt_pts:%d\n",dst->pkt_pts                );
//    LOGD("dst pkt_dts:%d\n",dst->pkt_dts                );
//    LOGD("dst pkt_pos:%d\n",dst->pkt_pos                );
//    LOGD("dst pkt_size:%d\n",dst->pkt_size               );
//    LOGD("dst pkt_duration:%d\n",dst->pkt_duration           );
//    LOGD("dst reordered_opaque:%d\n",dst->reordered_opaque       );
//    LOGD("dst quality:%d\n",dst->quality                );
//    LOGD("dst best_effort_timestamp:%d\n",dst->best_effort_timestamp  );
//    LOGD("dst coded_picture_number:%d\n",dst->coded_picture_number   );
//    LOGD("dst display_picture_number:%d\n",dst->display_picture_number );
//    LOGD("dst decode_error_flags:%d\n",dst->decode_error_flags     );
//    LOGD("dst mb_type:%d s->mb_width:%d s->mb_height%d\n",src->mb_type,s->mb_width,s->mb_height    );
    if (s->avctx->me_threshold) {
//        if (!src->motion_val[0])
//           LOGD("AVFrame.motion_val not set!\n");
//        if (!src->mb_type)
//        	LOGD("AVFrame.mb_type not set!\n");
//        if (!src->ref_index[0])
//        	LOGD( "AVFrame.ref_index not set!\n");
//        if (src->motion_subsample_log2 != dst->motion_subsample_log2)
//        	LOGD("AVFrame.motion_subsample_log2 doesn't match! (%d!=%d)\n",
//                   src->motion_subsample_log2, dst->motion_subsample_log2);

        memcpy(dst->mb_type, src->mb_type,
               s->mb_stride * s->mb_height * sizeof(dst->mb_type[0]));

        for (i = 0; i < 2; i++) {
            int stride = ((16 * s->mb_width ) >>
                          src->motion_subsample_log2) + 1;
            int height = ((16 * s->mb_height) >> src->motion_subsample_log2);

            if (src->motion_val[i] &&
                src->motion_val[i] != dst->motion_val[i]) {
                memcpy(dst->motion_val[i], src->motion_val[i],
                       2 * stride * height * sizeof(int16_t));
            }
            if (src->ref_index[i] && src->ref_index[i] != dst->ref_index[i]) {
                memcpy(dst->ref_index[i], src->ref_index[i],
                       s->mb_stride * 4 * s->mb_height * sizeof(int8_t));
            }
        }
    }
}


static int load_input_picture(MpegEncContext *s, const AVFrame *pic_arg)
{
	LOGD("load_input_picture Called\n");
	Picture *pic = NULL;
    int64_t pts;
    int i, display_picture_number = 0;
    const int encoding_delay = s->max_b_frames ? s->max_b_frames :
                                                 (s->low_delay ? 0 : 1);
//    LOGD("load_input_picture pic_arg:%d encoding_delay:%d\n",pic_arg,encoding_delay);
    int direct = 1;

    if (pic_arg) {
        pts = pic_arg->pts;
        display_picture_number = s->input_picture_number++;

        if (pts != AV_NOPTS_VALUE) {
            if (s->user_specified_pts != AV_NOPTS_VALUE) {
                int64_t time = pts;
                int64_t last = s->user_specified_pts;

                if (time <= last) {
//                   printf( "Error, Invalid timestamp=%"PRId64", ""last=%"PRId64"\n", pts, s->user_specified_pts);
                    LOGD( "ERROR load_input_picture time <= last");
                	return -1;
                }

                if (!s->low_delay && display_picture_number == 1)
                    s->dts_delta = time - last;
            }
            s->user_specified_pts = pts;
        } else {
            if (s->user_specified_pts != AV_NOPTS_VALUE) {
                s->user_specified_pts =
                pts = s->user_specified_pts + 1;
//                printf("Warning: AVFrame.pts=? trying to guess (%"PRId64")\n",pts);
                LOGD("ERROR load_input_picture s->user_specified_pts != AV_NOPTS_VALUE");

            } else {
                pts = display_picture_number;
            }
        }
    }

    if (pic_arg) {
        if (encoding_delay && !(s->flags & CODEC_FLAG_INPUT_PRESERVED))
            direct = 0;
        if (pic_arg->linesize[0] != s->linesize)
            direct = 0;
        if (pic_arg->linesize[1] != s->uvlinesize)
            direct = 0;
        if (pic_arg->linesize[2] != s->uvlinesize)
            direct = 0;

        if (direct) {
            i = ff_find_unused_picture(s, 1);
            if (i < 0)
                return i;

            pic = &s->picture[i];//MAX32で空のフレームを持っている
            pic->f.reference = 3;

            for (i = 0; i < 4; i++) {//引数のフレームのデータがそのまま代入されている
                pic->f.data[i]     = pic_arg->data[i];
                pic->f.linesize[i] = pic_arg->linesize[i];
            }
            if (ff_alloc_picture(s,  pic, 1) < 0) {
                return -1;
            }
        } else {//少なくとも初回は通っている
            i = ff_find_unused_picture(s, 0);
            if (i < 0)
                return i;

            pic = &s->picture[i];
            pic->f.reference = 3;

            if (ff_alloc_picture(s, (Picture *) pic, 0) < 0) {
                return -1;
            }

            if (pic->f.data[0] + INPLACE_OFFSET == pic_arg->data[0] &&
                pic->f.data[1] + INPLACE_OFFSET == pic_arg->data[1] &&
                pic->f.data[2] + INPLACE_OFFSET == pic_arg->data[2]) {
                // empty
            } else {

//               	LOGD("load_input_picture pix_fmt:%d linesize[0]:%d uvlinesize:%d number:%d\n",s->avctx->pix_fmt,pic_arg->linesize[0],s->uvlinesize,display_picture_number);
            	int h_chroma_shift, v_chroma_shift;
                av_pix_fmt_get_chroma_sub_sample(s->avctx->pix_fmt,
                                                 &h_chroma_shift,
                                                 &v_chroma_shift);
                 for (i = 0; i < 3; i++) {
                    int src_stride = pic_arg->linesize[i];
                    int dst_stride = i ? s->uvlinesize : s->linesize;
                    int h_shift = i ? h_chroma_shift : 0;
                    int v_shift = i ? v_chroma_shift : 0;
                    int w = s->width  >> h_shift;
                    int h = s->height >> v_shift;
                    uint8_t *src = pic_arg->data[i];
                    uint8_t *dst = pic->f.data[i];


                    if (s->codec_id == AV_CODEC_ID_AMV && !(s->avctx->flags & CODEC_FLAG_EMU_EDGE)) {
                        h = ((s->height + 15)/16*16) >> v_shift;
                    }

                    if (!s->avctx->rc_buffer_size)
                        dst += INPLACE_OFFSET;

                    if (src_stride == dst_stride && src_stride != 0){
                        memcpy(dst, src, src_stride * h);
                    }
                    else if(src_stride != 0){
                        int h2 = h;
                        uint8_t *dst2 = dst;
                        while (h2--) {
                            memcpy(dst2, src, w);
                            dst2 += dst_stride;
                            src += src_stride;
                        }
                    }
//                    LOGD("load_input_picture src:%d dst:%d draw_edges w:%d h:%d h_shift:%d v_shift:%d\n",src_stride,dst_stride,w,h,h_shift,v_shift);
                    if ((s->width & 15) || (s->height & 15)) {
                        s->dsp.draw_edges(dst, dst_stride,
                                          w, h,
                                          16>>h_shift,
                                          16>>v_shift,
                                          EDGE_BOTTOM);
                    }
                }
            }
        }
//        LOGD("load_input_picture attributes Before\n");
        copy_picture_attributes(s, &pic->f, pic_arg);//ここでpict_type等がコピーされている
        pic->f.display_picture_number = display_picture_number;
        pic->f.pts = pts; // we set this here to avoid modifiying pic_arg
    }

    /* shift buffer entries */
    for (i = 1; i < MAX_PICTURE_COUNT /*s->encoding_delay + 1*/; i++)
        s->input_picture[i - 1] = s->input_picture[i];

    s->input_picture[encoding_delay] = (Picture*) pic;

//	LOGD("load_input_picture INPUT_DEBUG\n");
//    for(i = 0; i < 100; i++){
//    	LOGD("%02x ",pic_arg->data[0][i]);
//    }
//    LOGD("load_input_picture E\n");
    return 0;
}

static int skip_check(MpegEncContext *s, Picture *p, Picture *ref)
{
    int x, y, plane;
    int score = 0;
    int64_t score64 = 0;

    for (plane = 0; plane < 3; plane++) {
        const int stride = p->f.linesize[plane];
        const int bw = plane ? 1 : 2;
        for (y = 0; y < s->mb_height * bw; y++) {
            for (x = 0; x < s->mb_width * bw; x++) {
                int off = p->f.type == FF_BUFFER_TYPE_SHARED ? 0 : 16;
                uint8_t *dptr = p->f.data[plane] + 8 * (x + y * stride) + off;
                uint8_t *rptr = ref->f.data[plane] + 8 * (x + y * stride);
                int v   = s->dsp.frame_skip_cmp[1](s, dptr, rptr, stride, 8);

                switch (s->avctx->frame_skip_exp) {
                case 0: score    =  FFMAX(score, v);          break;
                case 1: score   += FFABS(v);                  break;
                case 2: score   += v * v;                     break;
                case 3: score64 += FFABS(v * v * (int64_t)v); break;
                case 4: score64 += v * v * (int64_t)(v * v);  break;
                }
            }
        }
    }

    if (score)
        score64 = score;

    if (score64 < s->avctx->frame_skip_threshold)
        return 1;
    if (score64 < ((s->avctx->frame_skip_factor * (int64_t)s->lambda) >> 8))
        return 1;

    return 0;
}


static int get_sae(uint8_t *src, int ref, int stride)
{
    int x,y;
    int acc = 0;

    for (y = 0; y < 16; y++) {
        for (x = 0; x < 16; x++) {
            acc += FFABS(src[x + y * stride] - ref);
        }
    }

    return acc;
}


static int get_intra_count(MpegEncContext *s, uint8_t *src,
                           uint8_t *ref, int stride)
{
    int x, y, w, h;
    int acc = 0;

    w = s->width  & ~15;
    h = s->height & ~15;

    for (y = 0; y < h; y += 16) {
        for (x = 0; x < w; x += 16) {
            int offset = x + y * stride;
            int sad  = s->dsp.sad[0](NULL, src + offset, ref + offset, stride,
                                     16);
            int mean = (s->dsp.pix_sum(src + offset, stride) + 128) >> 8;
            int sae  = get_sae(src + offset, mean, stride);

            acc += sae + 500 < sad;
        }
    }
    return acc;
}


const AVCodecDescriptor *avcodec_descriptor_get(enum AVCodecID id)
{
    int i;
    //codec_descriptorsのサイズが出せなかったので数えて定数
    for (i = 0; i < 355; i++)
        if (codec_descriptors[i].id == id)
            return &codec_descriptors[i];
    return NULL;
}

int avcodec_is_open(AVCodecContext *s)
{
    return !!s->internal;
}

int ff_unlock_avcodec(void)
{
//    av_assert0(ff_avcodec_locked);
    ff_avcodec_locked = 0;
    entangled_thread_counter--;
    if (ff_lockmgr_cb) {
        if ((*ff_lockmgr_cb)(&codec_mutex, AV_LOCK_RELEASE))
            return -1;
    }
    return 0;
}

int ff_lock_avcodec(AVCodecContext *log_ctx)
{
    if (ff_lockmgr_cb) {
        if ((*ff_lockmgr_cb)(&codec_mutex, AV_LOCK_OBTAIN))
            return -1;
    }
    entangled_thread_counter++;
    if (entangled_thread_counter != 1) {
        printf("Insufficient thread locking around avcodec_open/close()\n");
        ff_avcodec_locked = 1;
        ff_unlock_avcodec();
        return -1;
    }
//    av_assert0(!ff_avcodec_locked);
    ff_avcodec_locked = 1;
    return 0;
}



void avcodec_set_dimensions(AVCodecContext *s, int width, int height)
{
    s->coded_width  = width;
    s->coded_height = height;
    s->width        = -((-width ) >> s->lowres);
    s->height       = -((-height) >> s->lowres);
}






int ff_get_logical_cpus(AVCodecContext *avctx)
{
    int ret, nb_cpus = 1;
#if HAVE_SCHED_GETAFFINITY && defined(CPU_COUNT)
    cpu_set_t cpuset;

    CPU_ZERO(&cpuset);

    ret = sched_getaffinity(0, sizeof(cpuset), &cpuset);
    if (!ret) {
        nb_cpus = CPU_COUNT(&cpuset);
    }
#elif HAVE_GETPROCESSAFFINITYMASK//オリジナルはここが有効になっている
    DWORD_PTR proc_aff, sys_aff;
    ret = GetProcessAffinityMask(GetCurrentProcess(), &proc_aff, &sys_aff);
    if (ret)
        nb_cpus = av_popcount64(proc_aff);
#elif HAVE_SYSCTL && defined(HW_NCPU)
    int mib[2] = { CTL_HW, HW_NCPU };
    size_t len = sizeof(nb_cpus);

    ret = sysctl(mib, 2, &nb_cpus, &len, NULL, 0);
    if (ret == -1)
        nb_cpus = 0;
#elif HAVE_SYSCONF && defined(_SC_NPROC_ONLN)
    nb_cpus = sysconf(_SC_NPROC_ONLN);
#elif HAVE_SYSCONF && defined(_SC_NPROCESSORS_ONLN)
    nb_cpus = sysconf(_SC_NPROCESSORS_ONLN);
#endif
    printf( "detected %d logical cores\n", nb_cpus);

    if  (avctx->height)
        nb_cpus = FFMIN(nb_cpus, (avctx->height+15)/16);

    return nb_cpus;
}
//FIFO--------------

void av_fifo_reset(AVFifoBuffer *f)
{
    f->wptr = f->rptr = f->buffer;
    f->wndx = f->rndx = 0;
}

AVFifoBuffer *av_fifo_alloc(unsigned int size)
{
    AVFifoBuffer *f= av_mallocz(sizeof(AVFifoBuffer));
    if (!f)
        return NULL;
    f->buffer = av_malloc(size);
    f->end = f->buffer + size;
    av_fifo_reset(f);
    if (!f->buffer)
        av_freep(&f);
    return f;
}

void av_fifo_free(AVFifoBuffer *f)
{
    if (f) {
        av_freep(&f->buffer);
        av_free(f);
    }
}


void ff_frame_thread_encoder_free(AVCodecContext *avctx){
    int i;
    ThreadContext *c= avctx->internal->frame_thread_encoder;

    pthread_mutex_lock(&c->task_fifo_mutex);
    c->exit = 1;
    pthread_cond_broadcast(&c->task_fifo_cond);
    pthread_mutex_unlock(&c->task_fifo_mutex);

    for (i=0; i<avctx->thread_count; i++) {
         pthread_join(c->worker[i], NULL);
    }

    pthread_mutex_destroy(&c->task_fifo_mutex);
    pthread_mutex_destroy(&c->finished_task_mutex);
    pthread_mutex_destroy(&c->buffer_mutex);
    pthread_cond_destroy(&c->task_fifo_cond);
    pthread_cond_destroy(&c->finished_task_cond);
    av_fifo_free(c->task_fifo); c->task_fifo = NULL;
    av_freep(&avctx->internal->frame_thread_encoder);
}



void av_bprint_init_for_buffer(AVBPrint *buf, char *buffer, unsigned size)
{
    buf->str      = buffer;
    buf->len      = 0;
    buf->size     = size;
    buf->size_max = size;
    *buf->str = 0;
}

/**
 * Count number of bits set to one in x
 * @param x value to count bits of
 * @return the number of bits set to one in x
 */
static inline int av_popcount_c(uint32_t x)
{
    x -= (x >> 1) & 0x55555555;
    x = (x & 0x33333333) + ((x >> 2) & 0x33333333);
    x = (x + (x >> 4)) & 0x0F0F0F0F;
    x += x >> 8;
    return (x + (x >> 16)) & 0x3F;
}

/**
 * Count number of bits set to one in x
 * @param x value to count bits of
 * @return the number of bits set to one in x
 */
static inline int av_popcount64_c(uint64_t x)
{
    return av_popcount_c((uint32_t)x) + av_popcount_c(x >> 32);
}

int av_get_channel_layout_nb_channels(uint64_t channel_layout)
{
    return av_popcount64_c(channel_layout);
}


/**
 * Test if the print buffer is complete (not truncated).
 *
 * It may have been truncated due to a memory allocation failure
 * or the size_max limit (compare size and size_max if necessary).
 */
static inline int av_bprint_is_complete(AVBPrint *buf)
{
    return buf->len < buf->size;
}


static int av_bprint_alloc(AVBPrint *buf, unsigned room)
{
    char *old_str, *new_str;
    unsigned min_size, new_size;

    if (buf->size == buf->size_max)
        return -1;
    if (!av_bprint_is_complete(buf))
        return AVERROR_INVALIDDATA; /* it is already truncated anyway */
    min_size = buf->len + 1 + FFMIN(UINT_MAX - buf->len - 1, room);
    new_size = buf->size > buf->size_max / 2 ? buf->size_max : buf->size * 2;
    if (new_size < min_size)
        new_size = FFMIN(buf->size_max, min_size);
    old_str = av_bprint_is_allocated(buf) ? buf->str : NULL;
    new_str = av_realloc(old_str, new_size);
    if (!new_str)
        return -1;
    if (!old_str)
        memcpy(new_str, buf->str, buf->len + 1);
    buf->str  = new_str;
    buf->size = new_size;
    return 0;
}


static void av_bprint_grow(AVBPrint *buf, unsigned extra_len)
{
    /* arbitrary margin to avoid small overflows */
    extra_len = FFMIN(extra_len, UINT_MAX - 5 - buf->len);
    buf->len += extra_len;
    if (buf->size)
        buf->str[FFMIN(buf->len, buf->size - 1)] = 0;
}

void av_bprintf(AVBPrint *buf, const char *fmt, ...)
{
	unsigned room;
    char *dst;
    __builtin_va_list vl;
    int extra_len;

    while (1) {
        room = av_bprint_room(buf);
        dst = room ? buf->str + buf->len : NULL;
        va_start(vl, fmt);
        extra_len = vsnprintf(dst, room, fmt, vl);
        va_end(vl);
        if (extra_len <= 0)
            return;
        if (extra_len < room)
            break;
        if (av_bprint_alloc(buf, extra_len))
            break;
    }
    av_bprint_grow(buf, extra_len);
}

static const char *get_channel_name(int channel_id)
{
    if (channel_id < 0 || channel_id >= FF_ARRAY_ELEMS(channel_names))
        return NULL;
    return channel_names[channel_id].name;
}

void av_bprint_channel_layout(struct AVBPrint *bp,
                              int nb_channels, uint64_t channel_layout)
{
    int i;

    if (nb_channels <= 0)
        nb_channels = av_get_channel_layout_nb_channels(channel_layout);

    for (i = 0; i < FF_ARRAY_ELEMS(channel_layout_map); i++)
        if (nb_channels    == channel_layout_map[i].nb_channels &&
            channel_layout == channel_layout_map[i].layout) {
            av_bprintf(bp, "%s", channel_layout_map[i].name);
            return;
        }

    av_bprintf(bp, "%d channels", nb_channels);
    if (channel_layout) {
        int i, ch;
        av_bprintf(bp, " (");
        for (i = 0, ch = 0; i < 64; i++) {
            if ((channel_layout & (UINT64_C(1) << i))) {
                const char *name = get_channel_name(i);
                if (name) {
                    if (ch > 0)
                        av_bprintf(bp, "+");
                    av_bprintf(bp, "%s", name);
                }
                ch++;
            }
        }
        av_bprintf(bp, ")");
    }
}

void av_get_channel_layout_string(char *buf, int buf_size,
                                  int nb_channels, uint64_t channel_layout)
{
    AVBPrint bp;

    av_bprint_init_for_buffer(&bp, buf, buf_size);
    av_bprint_channel_layout(&bp, nb_channels, channel_layout);
}


/**
 * Set the threading algorithms used.
 *
 * Threading requires more than one thread.
 * Frame threading requires entire frames to be passed to the codec,
 * and introduces extra decoding delay, so is incompatible with low_delay.
 *
 * @param avctx The context.
 */
static void validate_thread_parameters(AVCodecContext *avctx)
{
    int frame_threading_supported = (avctx->codec->capabilities & CODEC_CAP_FRAME_THREADS)
                                && !(avctx->flags & CODEC_FLAG_TRUNCATED)
                                && !(avctx->flags & CODEC_FLAG_LOW_DELAY)
                                && !(avctx->flags2 & CODEC_FLAG2_CHUNKS);
    if (avctx->thread_count == 1) {
        avctx->active_thread_type = 0;
    } else if (frame_threading_supported && (avctx->thread_type & FF_THREAD_FRAME)) {
        avctx->active_thread_type = FF_THREAD_FRAME;
    } else if ((avctx->codec->capabilities & CODEC_CAP_SLICE_THREADS) &&
               (avctx->thread_type & FF_THREAD_SLICE)) {
        avctx->active_thread_type = FF_THREAD_SLICE;
    } else if (!(avctx->codec->capabilities & CODEC_CAP_AUTO_THREADS)) {
        avctx->thread_count       = 1;
        avctx->active_thread_type = 0;
    }

    if (avctx->thread_count > MAX_AUTO_THREADS)
        printf("Application has requested %d threads. Using a thread count greater than %d is not recommended.\n",
               avctx->thread_count, MAX_AUTO_THREADS);
}

static inline void avcodec_thread_park_workers(ThreadContext *c, int thread_count)
{
    while (c->current_job != thread_count + c->job_count)
        pthread_cond_wait(&c->last_job_cond, &c->current_job_lock);
    pthread_mutex_unlock(&c->current_job_lock);
}

static int avcodec_thread_execute(AVCodecContext *avctx, action_func* func, void *arg, int *ret, int job_count, int job_size)
{
	LOGD("encode.c/avcodec_thread_execute Called\n");
    ThreadContext *c= avctx->thread_opaque;
    int dummy_ret;

    if (!(avctx->active_thread_type&FF_THREAD_SLICE) || avctx->thread_count <= 1)
        return avcodec_default_execute(avctx, func, arg, ret, job_count, job_size);

    if (job_count <= 0)
        return 0;

    pthread_mutex_lock(&c->current_job_lock);

    c->current_job = avctx->thread_count;
    c->job_count = job_count;
    c->job_size = job_size;
    c->args = arg;
    c->func = func;
    if (ret) {
        c->rets = ret;
        c->rets_count = job_count;
    } else {
        c->rets = &dummy_ret;
        c->rets_count = 1;
    }
    c->current_execute++;
    pthread_cond_broadcast(&c->current_job_cond);

    avcodec_thread_park_workers(c, avctx->thread_count);

    return 0;
}

static int avcodec_thread_execute2(AVCodecContext *avctx, action_func2* func2, void *arg, int *ret, int job_count)
{
    ThreadContext *c= avctx->thread_opaque;
    c->func2 = func2;
    return avcodec_thread_execute(avctx, NULL, arg, ret, job_count, 0);
}


/// Releases the buffers that this decoding thread was the last user of.
static void release_delayed_buffers(PerThreadContext *p)
{
    FrameThreadContext *fctx = p->parent;

    while (p->num_released_buffers > 0) {
        AVFrame *f;

        pthread_mutex_lock(&fctx->buffer_mutex);
        f = &p->released_buffers[--p->num_released_buffers];
        free_progress(f);
        f->thread_opaque = NULL;

        f->owner->release_buffer(f->owner, f);
        pthread_mutex_unlock(&fctx->buffer_mutex);
    }
}

/// Waits for all threads to finish.
static void park_frame_worker_threads(FrameThreadContext *fctx, int thread_count)
{
    int i;

    for (i = 0; i < thread_count; i++) {
        PerThreadContext *p = &fctx->threads[i];

        if (p->state != STATE_INPUT_READY) {
            pthread_mutex_lock(&p->progress_mutex);
            while (p->state != STATE_INPUT_READY)
                pthread_cond_wait(&p->output_cond, &p->progress_mutex);
            pthread_mutex_unlock(&p->progress_mutex);
        }
        p->got_frame = 0;
    }
}


/**
 * Update the next thread's AVCodecContext with values from the reference thread's context.
 *
 * @param dst The destination context.
 * @param src The source context.
 * @param for_user 0 if the destination is a codec thread, 1 if the destination is the user's thread
 */
static int update_context_from_thread(AVCodecContext *dst, AVCodecContext *src, int for_user)
{
    int err = 0;

    if (dst != src) {
        dst->time_base = src->time_base;
        dst->width     = src->width;
        dst->height    = src->height;
        dst->pix_fmt   = src->pix_fmt;

        dst->coded_width  = src->coded_width;
        dst->coded_height = src->coded_height;

        dst->has_b_frames = src->has_b_frames;
        dst->idct_algo    = src->idct_algo;

        dst->bits_per_coded_sample = src->bits_per_coded_sample;
        dst->sample_aspect_ratio   = src->sample_aspect_ratio;
        dst->dtg_active_format     = src->dtg_active_format;

        dst->profile = src->profile;
        dst->level   = src->level;

        dst->bits_per_raw_sample = src->bits_per_raw_sample;
        dst->ticks_per_frame     = src->ticks_per_frame;
        dst->color_primaries     = src->color_primaries;

        dst->color_trc   = src->color_trc;
        dst->colorspace  = src->colorspace;
        dst->color_range = src->color_range;
        dst->chroma_sample_location = src->chroma_sample_location;
    }

    if (for_user) {
        dst->delay       = src->thread_count - 1;
        dst->coded_frame = src->coded_frame;
    } else {
        if (dst->codec->update_thread_context)
            err = dst->codec->update_thread_context(dst, src);
    }

    return err;
}



static void video_free_buffers(AVCodecContext *s)
{
    AVCodecInternal *avci = s->internal;
    int i, j;

    if (!avci->buffer)
        return;

    if (avci->buffer_count)
        LOGD("Found %i unreleased buffers!\n",
               avci->buffer_count);
    for (i = 0; i < INTERNAL_BUFFER_SIZE; i++) {
        InternalBuffer *buf = &avci->buffer[i];
        for (j = 0; j < 4; j++) {
            av_freep(&buf->base[j]);
            buf->data[j] = NULL;
        }
    }
    av_freep(&avci->buffer);

    avci->buffer_count = 0;
}
static void audio_free_buffers(AVCodecContext *avctx)
{
    AVCodecInternal *avci = avctx->internal;
    av_freep(&avci->audio_data);
}

void avcodec_default_free_buffers(AVCodecContext *avctx)
{
    switch (avctx->codec_type) {
    case AVMEDIA_TYPE_VIDEO:
        video_free_buffers(avctx);
        break;
    case AVMEDIA_TYPE_AUDIO:
        audio_free_buffers(avctx);
        break;
    default:
        break;
    }
}

static void frame_thread_free(AVCodecContext *avctx, int thread_count)
{
    FrameThreadContext *fctx = avctx->thread_opaque;
    const AVCodec *codec = avctx->codec;
    int i;

    park_frame_worker_threads(fctx, thread_count);

    if (fctx->prev_thread && fctx->prev_thread != fctx->threads)
        if (update_context_from_thread(fctx->threads->avctx, fctx->prev_thread->avctx, 0) < 0) {
            printf( "Final thread update failed\n");
            fctx->prev_thread->avctx->internal->is_copy = fctx->threads->avctx->internal->is_copy;
            fctx->threads->avctx->internal->is_copy = 1;
        }

    fctx->die = 1;

    for (i = 0; i < thread_count; i++) {
        PerThreadContext *p = &fctx->threads[i];

        pthread_mutex_lock(&p->mutex);
        pthread_cond_signal(&p->input_cond);
        pthread_mutex_unlock(&p->mutex);

        if (p->thread_init)
            pthread_join(p->thread, NULL);
        p->thread_init=0;

        if (codec->close)
            codec->close(p->avctx);

        avctx->codec = NULL;

        release_delayed_buffers(p);
    }

    for (i = 0; i < thread_count; i++) {
        PerThreadContext *p = &fctx->threads[i];

        avcodec_default_free_buffers(p->avctx);

        pthread_mutex_destroy(&p->mutex);
        pthread_mutex_destroy(&p->progress_mutex);
        pthread_cond_destroy(&p->input_cond);
        pthread_cond_destroy(&p->progress_cond);
        pthread_cond_destroy(&p->output_cond);
        av_freep(&p->avpkt.data);

        if (i) {
            av_freep(&p->avctx->priv_data);
            av_freep(&p->avctx->internal);
            av_freep(&p->avctx->slice_offset);
        }

        av_freep(&p->avctx);
    }

    av_freep(&fctx->threads);
    pthread_mutex_destroy(&fctx->buffer_mutex);
    av_freep(&avctx->thread_opaque);
}


static void thread_free(AVCodecContext *avctx)
{
    ThreadContext *c = avctx->thread_opaque;
    int i;

    pthread_mutex_lock(&c->current_job_lock);
    c->done = 1;
    pthread_cond_broadcast(&c->current_job_cond);
    pthread_mutex_unlock(&c->current_job_lock);

    for (i=0; i<avctx->thread_count; i++)
         pthread_join(c->workers[i], NULL);

    pthread_mutex_destroy(&c->current_job_lock);
    pthread_cond_destroy(&c->current_job_cond);
    pthread_cond_destroy(&c->last_job_cond);
    av_free(c->workers);
    av_freep(&avctx->thread_opaque);
}

void ff_thread_free(AVCodecContext *avctx)
{
    if (avctx->active_thread_type&FF_THREAD_FRAME)
        frame_thread_free(avctx, avctx->thread_count);
    else
        thread_free(avctx);
}

static int thread_init(AVCodecContext *avctx)
{
    int i;
    ThreadContext *c;
    int thread_count = avctx->thread_count;

    if (!thread_count) {
        int nb_cpus = ff_get_logical_cpus(avctx);
        // use number of cores + 1 as thread count if there is more than one
        if (nb_cpus > 1)
            thread_count = avctx->thread_count = FFMIN(nb_cpus + 1, MAX_AUTO_THREADS);
        else
            thread_count = avctx->thread_count = 1;
    }

    if (thread_count <= 1) {
        avctx->active_thread_type = 0;
        return 0;
    }

    c = av_mallocz(sizeof(ThreadContext));
    if (!c)
        return -1;

    c->workers = av_mallocz(sizeof(pthread_t)*thread_count);
    if (!c->workers) {
        av_free(c);
        return -1;
    }

    avctx->thread_opaque = c;
    c->current_job = 0;
    c->job_count = 0;
    c->job_size = 0;
    c->done = 0;
    pthread_cond_init(&c->current_job_cond, NULL);
    pthread_cond_init(&c->last_job_cond, NULL);
    pthread_mutex_init(&c->current_job_lock, NULL);
    pthread_mutex_lock(&c->current_job_lock);
    for (i=0; i<thread_count; i++) {
        if(pthread_create(&c->workers[i], NULL, c->worker, avctx)) {//第三引数がworkerで何故か構造体の参照でなかったので参照にしちゃった
           avctx->thread_count = i;
           pthread_mutex_unlock(&c->current_job_lock);
           ff_thread_free(avctx);
           return -1;
        }
    }

    avcodec_thread_park_workers(c, thread_count);

    avctx->execute = avcodec_thread_execute;
    avctx->execute2 = avcodec_thread_execute2;//フレームがIフレームじゃない場合呼ばれる
    return 0;
}

static int frame_thread_init(AVCodecContext *avctx)
{
    int thread_count = avctx->thread_count;
    const AVCodec *codec = avctx->codec;
    AVCodecContext *src = avctx;
    FrameThreadContext *fctx;
    int i, err = 0;

    if (!thread_count) {
        int nb_cpus = ff_get_logical_cpus(avctx);
        if ((avctx->debug & (FF_DEBUG_VIS_QP | FF_DEBUG_VIS_MB_TYPE)) || avctx->debug_mv)
            nb_cpus = 1;
        // use number of cores + 1 as thread count if there is more than one
        if (nb_cpus > 1)
            thread_count = avctx->thread_count = FFMIN(nb_cpus + 1, MAX_AUTO_THREADS);
        else
            thread_count = avctx->thread_count = 1;
    }

    if (thread_count <= 1) {
        avctx->active_thread_type = 0;
        return 0;
    }

    avctx->thread_opaque = fctx = av_mallocz(sizeof(FrameThreadContext));

    fctx->threads = av_mallocz(sizeof(PerThreadContext) * thread_count);
    pthread_mutex_init(&fctx->buffer_mutex, NULL);
    fctx->delaying = 1;

    for (i = 0; i < thread_count; i++) {
        AVCodecContext *copy = av_malloc(sizeof(AVCodecContext));
        PerThreadContext *p  = &fctx->threads[i];

        pthread_mutex_init(&p->mutex, NULL);
        pthread_mutex_init(&p->progress_mutex, NULL);
        pthread_cond_init(&p->input_cond, NULL);
        pthread_cond_init(&p->progress_cond, NULL);
        pthread_cond_init(&p->output_cond, NULL);

        p->parent = fctx;
        p->avctx  = copy;

        if (!copy) {
            err = -1;
            goto error;
        }

        *copy = *src;
        copy->thread_opaque = p;
        copy->pkt = &p->avpkt;

        if (!i) {
            src = copy;

            if (codec->init)
                err = codec->init(copy);

            update_context_from_thread(avctx, copy, 1);
        } else {
            copy->priv_data = av_malloc(codec->priv_data_size);
            if (!copy->priv_data) {
                err = -1;
                goto error;
            }
            memcpy(copy->priv_data, src->priv_data, codec->priv_data_size);
            copy->internal = av_malloc(sizeof(AVCodecInternal));
            if (!copy->internal) {
                err = -1;
                goto error;
            }
            *copy->internal = *src->internal;
            copy->internal->is_copy = 1;

            if (codec->init_thread_copy)
                err = codec->init_thread_copy(copy);
        }

        if (err) goto error;

        err = -1;
        p->thread_init= !err;
        if(!p->thread_init)
            goto error;
    }

    return 0;

error:
    frame_thread_free(avctx, i+1);

    return err;
}


int ff_thread_init(AVCodecContext *avctx)
{
    if (avctx->thread_opaque) {
        printf( "avcodec_thread_init is ignored after avcodec_open\n");
        return -1;
    }

#if HAVE_W32THREADS
//    w32thread_init();
#endif

    if (avctx->codec) {
        validate_thread_parameters(avctx);

        if (avctx->active_thread_type&FF_THREAD_SLICE)
            return thread_init(avctx);
        else if (avctx->active_thread_type&FF_THREAD_FRAME)
            return frame_thread_init(avctx);
    }

    return 0;
}



enum AVSampleFormat av_get_planar_sample_fmt(enum AVSampleFormat sample_fmt)
{
    if (sample_fmt < 0 || sample_fmt >= AV_SAMPLE_FMT_NB)
        return AV_SAMPLE_FMT_NONE;
    if (sample_fmt_info[sample_fmt].planar)
        return sample_fmt;
    return sample_fmt_info[sample_fmt].altform;
}



const char *av_get_sample_fmt_name(enum AVSampleFormat sample_fmt)
{
    if (sample_fmt < 0 || sample_fmt >= AV_SAMPLE_FMT_NB)
        return NULL;
    return sample_fmt_info[sample_fmt].name;
}


//int av_get_exact_bits_per_sample(enum AVCodecID codec_id)
//{
//    switch (codec_id) {
//    case AV_CODEC_ID_8SVX_EXP:
//    case AV_CODEC_ID_8SVX_FIB:
//    case AV_CODEC_ID_ADPCM_CT:
//    case AV_CODEC_ID_ADPCM_IMA_APC:
//    case AV_CODEC_ID_ADPCM_IMA_EA_SEAD:
//    case AV_CODEC_ID_ADPCM_IMA_OKI:
//    case AV_CODEC_ID_ADPCM_IMA_WS:
//    case AV_CODEC_ID_ADPCM_G722:
//    case AV_CODEC_ID_ADPCM_YAMAHA:
//        return 4;
//    case AV_CODEC_ID_PCM_ALAW:
//    case AV_CODEC_ID_PCM_MULAW:
//    case AV_CODEC_ID_PCM_S8:
//    case AV_CODEC_ID_PCM_S8_PLANAR:
//    case AV_CODEC_ID_PCM_U8:
//    case AV_CODEC_ID_PCM_ZORK:
//        return 8;
//    case AV_CODEC_ID_PCM_S16BE:
//    case AV_CODEC_ID_PCM_S16BE_PLANAR:
//    case AV_CODEC_ID_PCM_S16LE:
//    case AV_CODEC_ID_PCM_S16LE_PLANAR:
//    case AV_CODEC_ID_PCM_U16BE:
//    case AV_CODEC_ID_PCM_U16LE:
//        return 16;
//    case AV_CODEC_ID_PCM_S24DAUD:
//    case AV_CODEC_ID_PCM_S24BE:
//    case AV_CODEC_ID_PCM_S24LE:
//    case AV_CODEC_ID_PCM_S24LE_PLANAR:
//    case AV_CODEC_ID_PCM_U24BE:
//    case AV_CODEC_ID_PCM_U24LE:
//        return 24;
//    case AV_CODEC_ID_PCM_S32BE:
//    case AV_CODEC_ID_PCM_S32LE:
//    case AV_CODEC_ID_PCM_S32LE_PLANAR:
//    case AV_CODEC_ID_PCM_U32BE:
//    case AV_CODEC_ID_PCM_U32LE:
//    case AV_CODEC_ID_PCM_F32BE:
//    case AV_CODEC_ID_PCM_F32LE:
//        return 32;
//    case AV_CODEC_ID_PCM_F64BE:
//    case AV_CODEC_ID_PCM_F64LE:
//        return 64;
//    default:
//        return 0;
//    }
//}

//int av_get_bits_per_sample(enum AVCodecID codec_id)
//{
//    switch (codec_id) {
//    case AV_CODEC_ID_ADPCM_SBPRO_2:
//        return 2;
//    case AV_CODEC_ID_ADPCM_SBPRO_3:
//        return 3;
//    case AV_CODEC_ID_ADPCM_SBPRO_4:
//    case AV_CODEC_ID_ADPCM_IMA_WAV:
//    case AV_CODEC_ID_ADPCM_IMA_QT:
//    case AV_CODEC_ID_ADPCM_SWF:
//    case AV_CODEC_ID_ADPCM_MS:
//        return 4;
//    default:
//        return av_get_exact_bits_per_sample(codec_id);
//    }
//}

static int get_bit_rate(AVCodecContext *ctx)
{
    int bit_rate;
    int bits_per_sample;

    switch (ctx->codec_type) {
    case AVMEDIA_TYPE_VIDEO:
    case AVMEDIA_TYPE_DATA:
    case AVMEDIA_TYPE_SUBTITLE:
    case AVMEDIA_TYPE_ATTACHMENT:
        bit_rate = ctx->bit_rate;
        break;
    case AVMEDIA_TYPE_AUDIO:
//        bits_per_sample = av_get_bits_per_sample(ctx->codec_id);
    	bits_per_sample = 16;
        bit_rate = bits_per_sample ? ctx->sample_rate * ctx->channels * bits_per_sample : ctx->bit_rate;
        break;
    default:
        bit_rate = 0;
        break;
    }
    return bit_rate;
}


int av_codec_is_encoder(const AVCodec *codec)
{
    return codec && (codec->encode_sub || codec->encode2);
}
/**
 * Return x default pointer in case p is NULL.
 */
static inline void *av_x_if_null(const void *p, const void *x)
{
    return (void *)(intptr_t)(p ? p : x);
}

int avcodec_open2(AVCodecContext *avctx, const AVCodec *codec, AVDictionary **options)
{
    LOGD("libavcodec/utils#avcodec_open2 Called\n");
    int ret = 0;
    AVDictionary *tmp = NULL;

    if (avcodec_is_open(avctx))
        return 0;

    if ((!codec && !avctx->codec)) {
    	LOGD( "libavcodec/utils No codec provided to avcodec_open2()\n");
        return -1;
    }
    if ((codec && avctx->codec && codec != avctx->codec)) {
    	LOGD( "libavcodec/utils This AVCodecContext was allocated for %s, "
                                    "but %s passed to avcodec_open2()\n", avctx->codec->name, codec->name);
        return -1;
    }
    if (!codec)
        codec = avctx->codec;

    if (avctx->extradata_size < 0 || avctx->extradata_size >= FF_MAX_EXTRADATA_SIZE)
        return -1;

//    if (options)
//        av_dict_copy(&tmp, *options, 0);

    ret = ff_lock_avcodec(avctx);
    if (ret < 0)
        return ret;

    avctx->internal = av_mallocz(sizeof(AVCodecInternal));
    if (!avctx->internal) {
        ret = -1;
        goto end;
    }

    if (codec->priv_data_size > 0) {
        if (!avctx->priv_data) {
            avctx->priv_data = av_mallocz(codec->priv_data_size);
            if (!avctx->priv_data) {
                ret = -1;
                goto end;
            }
//            if (codec->priv_class) {
//                *(const AVClass **)avctx->priv_data = codec->priv_class;
//                av_opt_set_defaults(avctx->priv_data);
//            }
        }
//        if ((ret = av_opt_set_dict(avctx->priv_data, &tmp)) < 0)
//            goto free_and_end;
    } else {
        avctx->priv_data = NULL;
    }
//    if ((ret = av_opt_set_dict(avctx, &tmp)) < 0)
//        goto free_and_end;

    //We only call avcodec_set_dimensions() for non h264 codecs so as not to overwrite previously setup dimensions
    if (!( avctx->coded_width && avctx->coded_height && avctx->width && avctx->height && avctx->codec_id == AV_CODEC_ID_H264)){

    if (avctx->coded_width && avctx->coded_height)
        avcodec_set_dimensions(avctx, avctx->coded_width, avctx->coded_height);
    else if (avctx->width && avctx->height)
        avcodec_set_dimensions(avctx, avctx->width, avctx->height);
    }

    if ((avctx->coded_width || avctx->coded_height || avctx->width || avctx->height)
        && (  av_image_check_size(avctx->coded_width, avctx->coded_height, 0, avctx) < 0
           || av_image_check_size(avctx->width,       avctx->height,       0, avctx) < 0)) {
        printf( "libavcodec/utils Ignoring invalid width/height values\n");
        avcodec_set_dimensions(avctx, 0, 0);
    }

    /* if the decoder init function was already called previously,
     * free the already allocated subtitle_header before overwriting it */
    if (av_codec_is_decoder(codec))
        av_freep(&avctx->subtitle_header);

    if (avctx->channels > FF_SANE_NB_CHANNELS) {
        ret = -1;
        goto free_and_end;
    }

    avctx->codec = codec;
    if ((avctx->codec_type == AVMEDIA_TYPE_UNKNOWN || avctx->codec_type == codec->type) &&
        avctx->codec_id == AV_CODEC_ID_NONE) {
        avctx->codec_type = codec->type;
        avctx->codec_id   = codec->id;
    }
    if (avctx->codec_id != codec->id || (avctx->codec_type != codec->type
                                         && avctx->codec_type != AVMEDIA_TYPE_ATTACHMENT)) {
        printf( "libavcodec/utils Codec type or id mismatches\n");
        ret = -1;
        goto free_and_end;
    }
    avctx->frame_number = 0;
    avctx->codec_descriptor = avcodec_descriptor_get(avctx->codec_id);

    if ((avctx->codec->capabilities & CODEC_CAP_EXPERIMENTAL) &&
        avctx->strict_std_compliance > FF_COMPLIANCE_EXPERIMENTAL) {
        const char *codec_string = av_codec_is_encoder(codec) ? "encoder" : "decoder";
        AVCodec *codec2;
        LOGD(
               "libavcodec/utils The %s '%s' is experimental but experimental codecs are not enabled, "
               "add '-strict %d' if you want to use it.\n",
               codec_string, codec->name, FF_COMPLIANCE_EXPERIMENTAL);
        codec2 = av_codec_is_encoder(codec) ? avcodec_find_encoder(codec->id) : avcodec_find_decoder(codec->id);
        if (!(codec2->capabilities & CODEC_CAP_EXPERIMENTAL))
        	LOGD( "libavcodec/utils Alternatively use the non experimental %s '%s'.\n",
                codec_string, codec2->name);
        ret = AVERROR_EXPERIMENTAL;
        goto free_and_end;
    }

    if (avctx->codec_type == AVMEDIA_TYPE_AUDIO &&
        (!avctx->time_base.num || !avctx->time_base.den)) {
        avctx->time_base.num = 1;
        avctx->time_base.den = avctx->sample_rate;
    }

    if (!HAVE_THREADS)
        printf( "libavcodec/utils Warning: not compiled with thread support, using thread emulation\n");

    if (HAVE_THREADS) {
        ff_unlock_avcodec(); //we will instanciate a few encoders thus kick the counter to prevent false detection of a problem
        ret = ff_frame_thread_encoder_init(avctx, options ? *options : NULL);
        ff_lock_avcodec(avctx);
        if (ret < 0)
            goto free_and_end;
    }

    if (HAVE_THREADS && !avctx->thread_opaque
        && !(avctx->internal->frame_thread_encoder && (avctx->active_thread_type&FF_THREAD_FRAME))) {
        ret = ff_thread_init(avctx);
        if (ret < 0) {
            goto free_and_end;
        }
    }
    if (!HAVE_THREADS && !(codec->capabilities & CODEC_CAP_AUTO_THREADS))
        avctx->thread_count = 1;

    if (avctx->codec->max_lowres < avctx->lowres || avctx->lowres < 0) {
        printf( "libavcodec/utils The maximum value for lowres supported by the decoder is %d\n",
               avctx->codec->max_lowres);
        ret = -1;
        goto free_and_end;
    }

    if (av_codec_is_encoder(avctx->codec)) {
        int i;
        if (avctx->codec->sample_fmts) {
            for (i = 0; avctx->codec->sample_fmts[i] != AV_SAMPLE_FMT_NONE; i++) {
                if (avctx->sample_fmt == avctx->codec->sample_fmts[i])
                    break;
                if (avctx->channels == 1 &&
                    av_get_planar_sample_fmt(avctx->sample_fmt) ==
                    av_get_planar_sample_fmt(avctx->codec->sample_fmts[i])) {
                    avctx->sample_fmt = avctx->codec->sample_fmts[i];
                    break;
                }
            }
            if (avctx->codec->sample_fmts[i] == AV_SAMPLE_FMT_NONE) {
                char buf[128];
                snprintf(buf, sizeof(buf), "%d", avctx->sample_fmt);
                LOGD( "libavcodec/utils Specified sample format %s is invalid or not supported\n",
                       (char *)av_x_if_null(av_get_sample_fmt_name(avctx->sample_fmt), buf));
                ret = -1;
                goto free_and_end;
            }
        }
        if (avctx->codec->pix_fmts) {
            for (i = 0; avctx->codec->pix_fmts[i] != AV_PIX_FMT_NONE; i++)
                if (avctx->pix_fmt == avctx->codec->pix_fmts[i])
                    break;
            if (avctx->codec->pix_fmts[i] == AV_PIX_FMT_NONE
                && !((avctx->codec_id == AV_CODEC_ID_MJPEG || avctx->codec_id == AV_CODEC_ID_LJPEG)
                     && avctx->strict_std_compliance <= FF_COMPLIANCE_UNOFFICIAL)) {
                char buf[128];
                snprintf(buf, sizeof(buf), "%d", avctx->pix_fmt);
                LOGD( "libavcodec/utils Specified pixel format %s is invalid or not supported\n",
                       (char *)av_x_if_null(av_get_pix_fmt_name(avctx->pix_fmt), buf));
                ret = -1;
                goto free_and_end;
            }
        }
        if (avctx->codec->supported_samplerates) {
            for (i = 0; avctx->codec->supported_samplerates[i] != 0; i++)
                if (avctx->sample_rate == avctx->codec->supported_samplerates[i])
                    break;
            if (avctx->codec->supported_samplerates[i] == 0) {
                printf( "libavcodec/utils Specified sample rate %d is not supported\n",
                       avctx->sample_rate);
                ret = -1;
                goto free_and_end;
            }
        }
        if (avctx->codec->channel_layouts) {
            if (!avctx->channel_layout) {
                printf( "libavcodec/utils Channel layout not specified\n");
            } else {
                for (i = 0; avctx->codec->channel_layouts[i] != 0; i++)
                    if (avctx->channel_layout == avctx->codec->channel_layouts[i])
                        break;
                if (avctx->codec->channel_layouts[i] == 0) {
                    char buf[512];
                    av_get_channel_layout_string(buf, sizeof(buf), -1, avctx->channel_layout);
                    printf( "libavcodec/utils Specified channel layout '%s' is not supported\n", buf);
                    ret = -1;
                    goto free_and_end;
                }
            }
        }
        if (avctx->channel_layout && avctx->channels) {
            int channels = av_get_channel_layout_nb_channels(avctx->channel_layout);
            if (channels != avctx->channels) {
                char buf[512];
                av_get_channel_layout_string(buf, sizeof(buf), -1, avctx->channel_layout);
                printf(
                       "libavcodec/utils Channel layout '%s' with %d channels does not match number of specified channels %d\n",
                       buf, channels, avctx->channels);
                ret = -1;
                goto free_and_end;
            }
        } else if (avctx->channel_layout) {
            avctx->channels = av_get_channel_layout_nb_channels(avctx->channel_layout);
        }
        if(avctx->codec_type == AVMEDIA_TYPE_VIDEO &&
           avctx->codec_id != AV_CODEC_ID_PNG // For mplayer
        ) {
            if (avctx->width <= 0 || avctx->height <= 0) {
                printf( "libavcodec/utils dimensions not set\n");
                ret = -1;
                goto free_and_end;
            }
        }
        if (   (avctx->codec_type == AVMEDIA_TYPE_VIDEO || avctx->codec_type == AVMEDIA_TYPE_AUDIO)
            && avctx->bit_rate>0 && avctx->bit_rate<1000) {
            printf( "libavcodec/utils Bitrate %d is extreemly low, did you mean %dk\n", avctx->bit_rate, avctx->bit_rate);
        }

        if (!avctx->rc_initial_buffer_occupancy)
            avctx->rc_initial_buffer_occupancy = avctx->rc_buffer_size * 3 / 4;
    }

    avctx->pts_correction_num_faulty_pts =
    avctx->pts_correction_num_faulty_dts = 0;
    avctx->pts_correction_last_pts =
    avctx->pts_correction_last_dts = INT64_MIN;

    if (   avctx->codec->init && (!(avctx->active_thread_type&FF_THREAD_FRAME)
        || avctx->internal->frame_thread_encoder)) {
        printf( "libavcodec/utils#avcodec_open2 avctx->codec->init(avctx)\n");
        ret = avctx->codec->init(avctx);
        if (ret < 0) {
            goto free_and_end;
        }
    }

    ret=0;

    if (av_codec_is_decoder(avctx->codec)) {
        if (!avctx->bit_rate)
            avctx->bit_rate = get_bit_rate(avctx);
        /* validate channel layout from the decoder */
        if (avctx->channel_layout) {
            int channels = av_get_channel_layout_nb_channels(avctx->channel_layout);
            if (!avctx->channels)
                avctx->channels = channels;
            else if (channels != avctx->channels) {
                char buf[512];
                av_get_channel_layout_string(buf, sizeof(buf), -1, avctx->channel_layout);
                printf(
                       "libavcodec/utils Channel layout '%s' with %d channels does not match specified number of channels %d: "
                       "ignoring specified channel layout\n",
                       buf, channels, avctx->channels);
                avctx->channel_layout = 0;
            }
        }
        if ((avctx->channels && avctx->channels < 0) ||
            avctx->channels > FF_SANE_NB_CHANNELS) {
            ret = -1;
            goto free_and_end;
        }
        if (avctx->sub_charenc) {
            if (avctx->codec_type != AVMEDIA_TYPE_SUBTITLE) {
                printf( "libavcodec/utils Character encoding is only "
                       "supported with subtitles codecs\n");
                ret = -1;
                goto free_and_end;
            } else if (avctx->codec_descriptor->props & AV_CODEC_PROP_BITMAP_SUB) {
                printf( "libavcodec/utils Codec '%s' is bitmap-based, "
                       "subtitles character encoding will be ignored\n",
                       avctx->codec_descriptor->name);
                avctx->sub_charenc_mode = FF_SUB_CHARENC_MODE_DO_NOTHING;
            } else {
                /* input character encoding is set for a text based subtitle
                 * codec at this point */
                if (avctx->sub_charenc_mode == FF_SUB_CHARENC_MODE_AUTOMATIC)
                    avctx->sub_charenc_mode = FF_SUB_CHARENC_MODE_PRE_DECODER;

                if (!CONFIG_ICONV && avctx->sub_charenc_mode == FF_SUB_CHARENC_MODE_PRE_DECODER) {
                    printf( "libavcodec/utils Character encoding subtitles "
                           "conversion needs a libavcodec built with iconv support "
                           "for this codec\n");
                    ret = -1;
                    goto free_and_end;
                }
            }
        }
    }
end:
    ff_unlock_avcodec();
    if (options) {
//        av_dict_free(options);
        *options = tmp;
    }

    return ret;
free_and_end:
//    av_dict_free(&tmp);
    av_freep(&avctx->priv_data);
    av_freep(&avctx->internal);
    avctx->codec = NULL;
    goto end;
    return 0;
}


av_cold int avcodec_close(AVCodecContext *avctx)
{
	LOGD("avcodec_close Called");
    int ret = ff_lock_avcodec(avctx);
    if (ret < 0)
        return ret;

	LOGD("avcodec_close A");
    if (avcodec_is_open(avctx)) {
        if (HAVE_THREADS && avctx->internal->frame_thread_encoder && avctx->thread_count > 1) {
            ff_unlock_avcodec();
            ff_frame_thread_encoder_free(avctx);
            ff_lock_avcodec(avctx);
        }
    	LOGD("avcodec_close B");
        if (HAVE_THREADS && avctx->thread_opaque)
            ff_thread_free(avctx);
        if (avctx->codec && avctx->codec->close)
            avctx->codec->close(avctx);
    	LOGD("avcodec_close C");
        avcodec_default_free_buffers(avctx);
        avctx->coded_frame = NULL;
        avctx->internal->byte_buffer_size = 0;
        av_freep(&avctx->internal->byte_buffer);
        av_freep(&avctx->internal);
    	LOGD("avcodec_close D");
//        av_dict_free(&avctx->metadata);
    }

	LOGD("avcodec_close E");
    if (avctx->priv_data && avctx->codec )
//        av_opt_free(avctx->priv_data);
//    av_opt_free(avctx);
    av_freep(&avctx->priv_data);
    if (av_codec_is_encoder(avctx->codec))
        av_freep(&avctx->extradata);
    avctx->codec = NULL;
    avctx->active_thread_type = 0;

	LOGD("avcodec_close F");
    ff_unlock_avcodec();
	LOGD("avcodec_close G");
    return 0;
}





// src must NOT be const as it can be a context for func that may need updating (like a pointer or byte counter)
int av_fifo_generic_write(AVFifoBuffer *f, void *src, int size, int (*func)(void*, void*, int))
{
    int total = size;
    uint32_t wndx= f->wndx;
    uint8_t *wptr= f->wptr;

    do {
        int len = FFMIN(f->end - wptr, size);
        if (func) {
            if (func(src, wptr, len) <= 0)
                break;
        } else {
            memcpy(wptr, src, len);
            src = (uint8_t*)src + len;
        }
// Write memory barrier needed for SMP here in theory
        wptr += len;
        if (wptr >= f->end)
            wptr = f->buffer;
        wndx += len;
        size -= len;
    } while (size > 0);
    f->wndx= wndx;
    f->wptr= wptr;
    return total - size;
}

void av_image_copy(uint8_t *dst_data[4], int dst_linesizes[4],
                   const uint8_t *src_data[4], const int src_linesizes[4],
                   enum AVPixelFormat pix_fmt, int width, int height)
{
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);

    if (!desc || (desc->flags & PIX_FMT_HWACCEL))
        return;

    if ((desc->flags & PIX_FMT_PAL) ||
        (desc->flags & PIX_FMT_PSEUDOPAL)) {
        av_image_copy_plane(dst_data[0], dst_linesizes[0],
                            src_data[0], src_linesizes[0],
                            width, height);
        /* copy the palette */
        memcpy(dst_data[1], src_data[1], 4*256);
    } else {
        int i, planes_nb = 0;

        for (i = 0; i < desc->nb_components; i++)
            planes_nb = FFMAX(planes_nb, desc->comp[i].plane + 1);

        for (i = 0; i < planes_nb; i++) {
            int h = height;
            int bwidth = av_image_get_linesize(pix_fmt, width, i);
            if (bwidth < 0) {
                printf("av_image_get_linesize failed\n");
                return;
            }
            if (i == 1 || i == 2) {
                h= -((-height)>>desc->log2_chroma_h);
            }
            av_image_copy_plane(dst_data[i], dst_linesizes[i],
                                src_data[i], src_linesizes[i],
                                bwidth, h);
        }
    }
}


//呼ばれてない
int ff_thread_video_encode_frame(AVCodecContext *avctx, AVPacket *pkt, const AVFrame *frame, int *got_packet_ptr){


    LOGD( "ff_thread_video_encode_frame Called\n");
    ThreadContext *c = avctx->internal->frame_thread_encoder;
    Task task;
    int ret;
//    av_assert1(!*got_packet_ptr);

    if(frame){
        if(!(avctx->flags & CODEC_FLAG_INPUT_PRESERVED)){
            AVFrame *new = avcodec_alloc_frame();
            if(!new)
                return -1;
            pthread_mutex_lock(&c->buffer_mutex);
            ret = ff_get_buffer(c->parent_avctx, new);
            pthread_mutex_unlock(&c->buffer_mutex);
            if(ret<0)
                return ret;
            new->pts = frame->pts;
            new->quality = frame->quality;
            new->pict_type = frame->pict_type;
            av_image_copy(new->data, new->linesize, (const uint8_t **)frame->data, frame->linesize,
                          avctx->pix_fmt, avctx->width, avctx->height);
            frame = new;
        }

        task.index = c->task_index;
        task.indata = (void*)frame;
        pthread_mutex_lock(&c->task_fifo_mutex);
        av_fifo_generic_write(c->task_fifo, &task, sizeof(task), NULL);
        pthread_cond_signal(&c->task_fifo_cond);
        pthread_mutex_unlock(&c->task_fifo_mutex);

        c->task_index = (c->task_index+1) % BUFFER_SIZE;

        if(!c->finished_tasks[c->finished_task_index].outdata && (c->task_index - c->finished_task_index) % BUFFER_SIZE <= avctx->thread_count)
            return 0;
    }

    if(c->task_index == c->finished_task_index)
        return 0;

    pthread_mutex_lock(&c->finished_task_mutex);
    while (!c->finished_tasks[c->finished_task_index].outdata) {
        pthread_cond_wait(&c->finished_task_cond, &c->finished_task_mutex);
    }
    task = c->finished_tasks[c->finished_task_index];
    *pkt = *(AVPacket*)(task.outdata);
    if(pkt->data)
        *got_packet_ptr = 1;
    av_freep(&c->finished_tasks[c->finished_task_index].outdata);
    c->finished_task_index = (c->finished_task_index+1) % BUFFER_SIZE;
    pthread_mutex_unlock(&c->finished_task_mutex);

    return task.return_code;
}


/* Makes duplicates of data, side_data, but does not copy any other fields */
static int copy_packet_data(AVPacket *dst, AVPacket *src)
{
//    LOGD("copy_packet_data A av_malloc size:%d",dst->size);
//    LOGD("copy_packet_data B av_malloc size:%d",dst->side_data_elems * sizeof(*dst->side_data));
    dst->data      = NULL;
    dst->side_data = NULL;
    DUP_DATA(dst->data, src->data, dst->size, 1);
    dst->destruct = av_destruct_packet;

    if (dst->side_data_elems) {
        int i;

        DUP_DATA(dst->side_data, src->side_data,
                dst->side_data_elems * sizeof(*dst->side_data), 0);
        memset(dst->side_data, 0,
                dst->side_data_elems * sizeof(*dst->side_data));
        for (i = 0; i < dst->side_data_elems; i++) {
            DUP_DATA(dst->side_data[i].data, src->side_data[i].data,
                    src->side_data[i].size, 1);
            dst->side_data[i].size = src->side_data[i].size;
            dst->side_data[i].type = src->side_data[i].type;
        }
    }
    return 0;

failed_alloc:
    av_destruct_packet(dst);
    return -1;
}


int av_dup_packet(AVPacket *pkt)
{
    AVPacket tmp_pkt;

    if (pkt->destruct == NULL && pkt->data) {
        tmp_pkt = *pkt;
        return copy_packet_data(pkt, &tmp_pkt);
    }
    return 0;
}


static int encode_frame(AVCodecContext *c, AVFrame *frame)
{
    AVPacket pkt = { 0 };
    int ret, got_output;

    av_init_packet(&pkt);
    ret = avcodec_encode_video2(c, &pkt, frame, &got_output);
    if (ret < 0)
        return ret;

    ret = pkt.size;
    av_free_packet(&pkt);
    return ret;
}

static int estimate_best_b_count(MpegEncContext *s)
{
    AVCodec *codec    = avcodec_find_encoder(s->avctx->codec_id);
    AVCodecContext *c = avcodec_alloc_context3(NULL);
    AVFrame input[FF_MAX_B_FRAMES + 2];
    const int scale = s->avctx->brd_scale;
    int i, j, out_size, p_lambda, b_lambda, lambda2;
    int64_t best_rd  = INT64_MAX;
    int best_b_count = -1;

//    av_assert0(scale >= 0 && scale <= 3);

    //emms_c();
    //s->next_picture_ptr->quality;
    p_lambda = s->last_lambda_for[AV_PICTURE_TYPE_P];
    //p_lambda * FFABS(s->avctx->b_quant_factor) + s->avctx->b_quant_offset;
    b_lambda = s->last_lambda_for[AV_PICTURE_TYPE_B];
    if (!b_lambda) // FIXME we should do this somewhere else
        b_lambda = p_lambda;
    lambda2  = (b_lambda * b_lambda + (1 << FF_LAMBDA_SHIFT) / 2) >>
               FF_LAMBDA_SHIFT;

    c->width        = s->width  >> scale;
    c->height       = s->height >> scale;
    c->flags        = CODEC_FLAG_QSCALE | CODEC_FLAG_PSNR |
                      CODEC_FLAG_INPUT_PRESERVED /*| CODEC_FLAG_EMU_EDGE*/;
    c->flags       |= s->avctx->flags & CODEC_FLAG_QPEL;
    c->mb_decision  = s->avctx->mb_decision;
    c->me_cmp       = s->avctx->me_cmp;
    c->mb_cmp       = s->avctx->mb_cmp;
    c->me_sub_cmp   = s->avctx->me_sub_cmp;
    c->pix_fmt      = AV_PIX_FMT_YUV420P;
    c->time_base    = s->avctx->time_base;
    c->max_b_frames = s->max_b_frames;

    if (avcodec_open2(c, codec, NULL) < 0)
        return -1;

    for (i = 0; i < s->max_b_frames + 2; i++) {
        int ysize = c->width * c->height;
        int csize = (c->width / 2) * (c->height / 2);
        Picture pre_input, *pre_input_ptr = i ? s->input_picture[i - 1] :
                                                s->next_picture_ptr;

        avcodec_get_frame_defaults(&input[i]);
        input[i].data[0]     = av_malloc(ysize + 2 * csize);
        input[i].data[1]     = input[i].data[0] + ysize;
        input[i].data[2]     = input[i].data[1] + csize;
        input[i].linesize[0] = c->width;
        input[i].linesize[1] =
        input[i].linesize[2] = c->width / 2;

        if (pre_input_ptr && (!i || s->input_picture[i - 1])) {
            pre_input = *pre_input_ptr;

            if (pre_input.f.type != FF_BUFFER_TYPE_SHARED && i) {
                pre_input.f.data[0] += INPLACE_OFFSET;
                pre_input.f.data[1] += INPLACE_OFFSET;
                pre_input.f.data[2] += INPLACE_OFFSET;
            }

            s->dsp.shrink[scale](input[i].data[0], input[i].linesize[0],
                                 pre_input.f.data[0], pre_input.f.linesize[0],
                                 c->width,      c->height);
            s->dsp.shrink[scale](input[i].data[1], input[i].linesize[1],
                                 pre_input.f.data[1], pre_input.f.linesize[1],
                                 c->width >> 1, c->height >> 1);
            s->dsp.shrink[scale](input[i].data[2], input[i].linesize[2],
                                 pre_input.f.data[2], pre_input.f.linesize[2],
                                 c->width >> 1, c->height >> 1);
        }
    }

    for (j = 0; j < s->max_b_frames + 1; j++) {
        int64_t rd = 0;

        if (!s->input_picture[j])
            break;

        c->error[0] = c->error[1] = c->error[2] = 0;

        input[0].pict_type = AV_PICTURE_TYPE_I;
        input[0].quality   = 1 * FF_QP2LAMBDA;

        out_size = encode_frame(c, &input[0]);

        //rd += (out_size * lambda2) >> FF_LAMBDA_SHIFT;

        for (i = 0; i < s->max_b_frames + 1; i++) {
            int is_p = i % (j + 1) == j || i == s->max_b_frames;

            input[i + 1].pict_type = is_p ?
                                     AV_PICTURE_TYPE_P : AV_PICTURE_TYPE_B;
            input[i + 1].quality   = is_p ? p_lambda : b_lambda;

            out_size = encode_frame(c, &input[i + 1]);

            rd += (out_size * lambda2) >> (FF_LAMBDA_SHIFT - 3);
        }

        /* get the delayed frames */
        while (out_size) {
            out_size = encode_frame(c, NULL);
            rd += (out_size * lambda2) >> (FF_LAMBDA_SHIFT - 3);
        }

        rd += c->error[0] + c->error[1] + c->error[2];

        if (rd < best_rd) {
            best_rd = rd;
            best_b_count = j;
        }
    }

    avcodec_close(c);
    av_freep(&c);

    for (i = 0; i < s->max_b_frames + 2; i++) {
        av_freep(&input[i].data[0]);
    }

    return best_b_count;
}

void ff_copy_picture(Picture *dst, Picture *src)
{
    *dst = *src;
    dst->f.type = FF_BUFFER_TYPE_COPY;
}

static int select_input_picture(MpegEncContext *s)
{
	LOGD("select_input_picture Called\n");
    int i;

    for (i = 1; i < MAX_PICTURE_COUNT; i++)
        s->reordered_input_picture[i - 1] = s->reordered_input_picture[i];
    s->reordered_input_picture[MAX_PICTURE_COUNT - 1] = NULL;

    /* set next picture type & ordering */
    if (s->reordered_input_picture[0] == NULL && s->input_picture[0]) {
        if (/*s->picture_in_gop_number >= s->gop_size ||*/
            s->next_picture_ptr == NULL || s->intra_only) {//ここでgop_sizeを気にしなくてもpict_typeはオリジナル通りになる
        	LOGD("ORDER__I_FRAME\n");
            s->reordered_input_picture[0] = s->input_picture[0];
            s->reordered_input_picture[0]->f.pict_type = AV_PICTURE_TYPE_I;
            s->reordered_input_picture[0]->f.coded_picture_number =
                s->coded_picture_number++;
        } else {
        	LOGD("ORDER_B_FRAME\n");
            int b_frames;

            if (s->avctx->frame_skip_threshold || s->avctx->frame_skip_factor) {
                if (s->picture_in_gop_number < s->gop_size &&
                    skip_check(s, s->input_picture[0], s->next_picture_ptr)) {
                    // FIXME check that te gop check above is +-1 correct
                    if (s->input_picture[0]->f.type == FF_BUFFER_TYPE_SHARED) {
                        for (i = 0; i < 4; i++)
                            s->input_picture[0]->f.data[i] = NULL;
                        s->input_picture[0]->f.type = 0;
                    } else {
//                        assert(s->input_picture[0]->f.type == FF_BUFFER_TYPE_USER ||
//                               s->input_picture[0]->f.type == FF_BUFFER_TYPE_INTERNAL);

                        s->avctx->release_buffer(s->avctx,
                                                 &s->input_picture[0]->f);
                    }
                    ff_vbv_update(s, 0);

                    goto no_output_pic;
                }
            }

            if (s->flags & CODEC_FLAG_PASS2) {
                for (i = 0; i < s->max_b_frames + 1; i++) {
                    int pict_num = s->input_picture[0]->f.display_picture_number + i;

                    if (pict_num >= s->rc_context.num_entries)
                        break;
                    if (!s->input_picture[i]) {
//                        LOGD("select_input_picture rc_context SET_P-FRAME\n");
                        s->rc_context.entry[pict_num - 1].new_pict_type = AV_PICTURE_TYPE_P;
                        break;
                    }

                    s->input_picture[i]->f.pict_type =
                        s->rc_context.entry[pict_num].new_pict_type;
                }
            }

            if (s->avctx->b_frame_strategy == 0) {
                b_frames = s->max_b_frames;
                while (b_frames && !s->input_picture[b_frames])
                    b_frames--;
            } else if (s->avctx->b_frame_strategy == 1) {
//            	LOGD("select_input_picture E max_b_frame:%d\n",s->max_b_frames);
                for (i = 1; i < s->max_b_frames + 1; i++) {
                    if (s->input_picture[i] &&
                        s->input_picture[i]->b_frame_score == 0) {
                        s->input_picture[i]->b_frame_score =
                            get_intra_count(s,
                                            s->input_picture[i    ]->f.data[0],
                                            s->input_picture[i - 1]->f.data[0],
                                            s->linesize) + 1;
                    }
                }
                for (i = 0; i < s->max_b_frames + 1; i++) {
                    if (s->input_picture[i] == NULL ||
                        s->input_picture[i]->b_frame_score - 1 >
                            s->mb_num / s->avctx->b_sensitivity)
                        break;
                }

                b_frames = FFMAX(0, i - 1);

                /* reset scores */
                for (i = 0; i < b_frames + 1; i++) {
                    s->input_picture[i]->b_frame_score = 0;
                }

            } else if (s->avctx->b_frame_strategy == 2) {
                b_frames = estimate_best_b_count(s);
            } else {
                LOGD( "illegal b frame strategy\n");
                b_frames = 0;
            }


            for (i = b_frames - 1; i >= 0; i--) {
                int type = s->input_picture[i]->f.pict_type;
                if (type && type != AV_PICTURE_TYPE_B)
                    b_frames = i;
            }
            if (s->input_picture[b_frames]->f.pict_type == AV_PICTURE_TYPE_B &&
                b_frames == s->max_b_frames) {
                LOGD( "warning, too many b frames in a row\n");
            }

            if (s->picture_in_gop_number + b_frames >= s->gop_size) {
                if ((s->mpv_flags & FF_MPV_FLAG_STRICT_GOP) &&
                    s->gop_size > s->picture_in_gop_number) {
                    b_frames = s->gop_size - s->picture_in_gop_number - 1;
                } else {
                    if (s->flags & CODEC_FLAG_CLOSED_GOP)
                        b_frames = 0;
                    s->input_picture[b_frames]->f.pict_type = AV_PICTURE_TYPE_I;
                }
            }

            if ((s->flags & CODEC_FLAG_CLOSED_GOP) && b_frames &&
                s->input_picture[b_frames]->f.pict_type == AV_PICTURE_TYPE_I)
                b_frames--;
            s->reordered_input_picture[0] = s->input_picture[b_frames];
            if (s->reordered_input_picture[0]->f.pict_type != AV_PICTURE_TYPE_I){
            	LOGD("select_input_picture J\n");
                s->reordered_input_picture[0]->f.pict_type = AV_PICTURE_TYPE_P;
            }
            s->reordered_input_picture[0]->f.coded_picture_number =
                s->coded_picture_number++;
            for (i = 0; i < b_frames; i++) {
                s->reordered_input_picture[i + 1] = s->input_picture[i];
                s->reordered_input_picture[i + 1]->f.pict_type =
                    AV_PICTURE_TYPE_B;
                s->reordered_input_picture[i + 1]->f.coded_picture_number =
                    s->coded_picture_number++;
            }
        }
    }
    //いいデバックポイント
    LOGD("select_input_picture pict_type:%d\n",s->reordered_input_picture[0]->f.pict_type);
no_output_pic:
    if (s->reordered_input_picture[0]) {
    	LOGD("select_input_picture reordered_input_picture SET_UNKNOWN_OR_P-FRAME\n");
        s->reordered_input_picture[0]->f.reference =
           s->reordered_input_picture[0]->f.pict_type !=
               AV_PICTURE_TYPE_B ? 3 : 0;

        ff_copy_picture(&s->new_picture, s->reordered_input_picture[0]);

        if (s->reordered_input_picture[0]->f.type == FF_BUFFER_TYPE_SHARED ||
            s->avctx->rc_buffer_size) {
            // input is a shared pix, so we can't modifiy it -> alloc a new
            // one & ensure that the shared one is reuseable

            Picture *pic;
            int i = ff_find_unused_picture(s, 0);
            if (i < 0)
                return i;
            pic = &s->picture[i];

            pic->f.reference = s->reordered_input_picture[0]->f.reference;
            if (ff_alloc_picture(s, pic, 0) < 0) {
                return -1;
            }

            /* mark us unused / free shared pic */
            if (s->reordered_input_picture[0]->f.type == FF_BUFFER_TYPE_INTERNAL)
                s->avctx->release_buffer(s->avctx,
                                         &s->reordered_input_picture[0]->f);
            for (i = 0; i < 4; i++)
                s->reordered_input_picture[0]->f.data[i] = NULL;
            s->reordered_input_picture[0]->f.type = 0;

            copy_picture_attributes(s, &pic->f,
                                    &s->reordered_input_picture[0]->f);

//            LOGD("STE CURRENT pic:%d\n",pic);
            s->current_picture_ptr = pic;
        } else {//初回こっちを通っている
            // input is not a shared pix -> reuse buffer for current_pix

//            assert(s->reordered_input_picture[0]->f.type ==
//                       FF_BUFFER_TYPE_USER ||
//                   s->reordered_input_picture[0]->f.type ==
//                       FF_BUFFER_TYPE_INTERNAL);

            //reorder～がcurrent_ptrに入ってcurrent_ptrが普通のcurrentに入る
            LOGD("TOTAL:%d s->reordered_input_picture[0]:%d mb_var_sum:%d\n",s->total_bits,s->reordered_input_picture[0],s->reordered_input_picture[0]->mb_var_sum);
            s->current_picture_ptr = s->reordered_input_picture[0];
            for (i = 0; i < 4; i++) {
                s->new_picture.f.data[i] += INPLACE_OFFSET;
            }
        }
        ff_copy_picture(&s->current_picture, s->current_picture_ptr);
        LOGD("TOTAL_Y:%d",s->total_bits);
        s->picture_number = s->new_picture.f.display_picture_number;
    } else {
        memset(&s->new_picture, 0, sizeof(Picture));
    }
    return 0;
}

int av_new_packet(AVPacket *pkt, int size)
{
    uint8_t *data = NULL;
    if ((unsigned)size < (unsigned)size + FF_INPUT_BUFFER_PADDING_SIZE)
    	LOGD("av_new_packet av_malloc size:%d",size+FF_INPUT_BUFFER_PADDING_SIZE);
        data = av_malloc(size + FF_INPUT_BUFFER_PADDING_SIZE);
    if (data) {
        memset(data + size, 0, FF_INPUT_BUFFER_PADDING_SIZE);
    } else
        size = 0;

    av_init_packet(pkt);
    pkt->data     = data;
    pkt->size     = size;
    pkt->destruct = av_destruct_packet;
    if (!data)
        return -1;
    return 0;
}


static inline int ff_fast_malloc(void *ptr, unsigned int *size, size_t min_size, int zero_realloc)
{
    void **p = ptr;
    if (min_size < *size)
        return 0;
    min_size = FFMAX(17 * min_size / 16 + 32, min_size);
    av_free(*p);
    *p = zero_realloc ? av_mallocz(min_size) : av_malloc(min_size);
    if (!*p)
        min_size = 0;
    *size = min_size;
    return 1;
}

void av_fast_padded_malloc(void *ptr, unsigned int *size, size_t min_size)
{
    uint8_t **p = ptr;
    if (min_size > SIZE_MAX - FF_INPUT_BUFFER_PADDING_SIZE) {
        av_freep(p);
        *size = 0;
        return;
    }
    if (!ff_fast_malloc(p, size, min_size + FF_INPUT_BUFFER_PADDING_SIZE, 1))
        memset(*p + min_size, 0, FF_INPUT_BUFFER_PADDING_SIZE);
}



uint8_t *av_packet_new_side_data(AVPacket *pkt, enum AVPacketSideDataType type,
                                 int size)
{
    LOGD("av_packet_new_side_data Called");
    int elems = pkt->side_data_elems;

    if ((unsigned)elems + 1 > INT_MAX / sizeof(*pkt->side_data))
        return NULL;
    if ((unsigned)size > INT_MAX - FF_INPUT_BUFFER_PADDING_SIZE)
        return NULL;
    pkt->side_data = av_realloc(pkt->side_data,
                                (elems + 1) * sizeof(*pkt->side_data));
    if (!pkt->side_data)
        return NULL;

    pkt->side_data[elems].data = av_malloc(size + FF_INPUT_BUFFER_PADDING_SIZE);
    if (!pkt->side_data[elems].data)
        return NULL;
    pkt->side_data[elems].size = size;
    pkt->side_data[elems].type = type;
    pkt->side_data_elems++;

    return pkt->side_data[elems].data;
}


void ff_thread_report_progress(AVFrame *f, int n, int field)
{
    PerThreadContext *p;
    volatile int *progress = f->thread_opaque;

    if (!progress || progress[field] >= n) return;

    p = f->owner->thread_opaque;

    if (f->owner->debug&FF_DEBUG_THREADS)
        printf( "%p finished %d field %d\n", progress, n, field);

    pthread_mutex_lock(&p->progress_mutex);
    progress[field] = n;
    pthread_cond_broadcast(&p->progress_cond);
    pthread_mutex_unlock(&p->progress_mutex);
}

/* generic function for encode/decode called after a
 * frame has been coded/decoded. */
void ff_MPV_frame_end(MpegEncContext *s)
{
//	LOGD("frame_end Called\n");
    int i;
    /* redraw edges for the frame if decoding didn't complete */
    // just to make sure that all data is rendered.
//    LOGD("ff_MPV_frame_end A:%d\n",(CONFIG_MPEG_XVMC_DECODER && s->avctx->xvmc_acceleration));
//    LOGD("ff_MPV_frame_end B:%d\n",((s->er.error_count || s->encoding || !(s->avctx->codec->capabilities&CODEC_CAP_DRAW_HORIZ_BAND)) &&
//            !s->avctx->hwaccel &&
//            !(s->avctx->codec->capabilities & CODEC_CAP_HWACCEL_VDPAU) &&
//            s->unrestricted_mv &&
//            s->current_picture.f.reference &&
//            !s->intra_only &&
//            !(s->flags & CODEC_FLAG_EMU_EDGE) &&
//            !s->avctx->lowres
//          ));
        if (CONFIG_MPEG_XVMC_DECODER && s->avctx->xvmc_acceleration) {
        ff_xvmc_field_end(s);
   } else if ((s->er.error_count || s->encoding || !(s->avctx->codec->capabilities&CODEC_CAP_DRAW_HORIZ_BAND)) &&
              !s->avctx->hwaccel &&
              !(s->avctx->codec->capabilities & CODEC_CAP_HWACCEL_VDPAU) &&
              s->unrestricted_mv &&
              s->current_picture.f.reference &&
              !s->intra_only &&
              !(s->flags & CODEC_FLAG_EMU_EDGE) &&
              !s->avctx->lowres
            ) {
	   //s->avctx->pix_fmtはAV_PIX_FMT_NV21じゃなくてAV_PIX_FMT_YUV420PでOK
        const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(s->avctx->pix_fmt);
        int hshift = desc->log2_chroma_w;
        int vshift = desc->log2_chroma_h;
        s->dsp.draw_edges(s->current_picture.f.data[0], s->current_picture.f.linesize[0],
                          s->h_edge_pos, s->v_edge_pos,
                          EDGE_WIDTH, EDGE_WIDTH,
                          EDGE_TOP | EDGE_BOTTOM);
        s->dsp.draw_edges(s->current_picture.f.data[1], s->current_picture.f.linesize[1],
                          s->h_edge_pos >> hshift, s->v_edge_pos >> vshift,
                          EDGE_WIDTH >> hshift, EDGE_WIDTH >> vshift,
                          EDGE_TOP | EDGE_BOTTOM);
        s->dsp.draw_edges(s->current_picture.f.data[2], s->current_picture.f.linesize[2],
                          s->h_edge_pos >> hshift, s->v_edge_pos >> vshift,
                          EDGE_WIDTH >> hshift, EDGE_WIDTH >> vshift,
                          EDGE_TOP | EDGE_BOTTOM);
      }
//データがあるかどうか

    s->last_pict_type                 = s->pict_type;
    s->last_lambda_for [s->pict_type] = s->current_picture_ptr->f.quality;
    if (s->pict_type!= AV_PICTURE_TYPE_B) {
        s->last_non_b_pict_type = s->pict_type;
    }
#if 0
    /* copy back current_picture variables */
    for (i = 0; i < MAX_PICTURE_COUNT; i++) {
        if (s->picture[i].f.data[0] == s->current_picture.f.data[0]) {
            s->picture[i] = s->current_picture;
            break;
        }
    }
//    assert(i < MAX_PICTURE_COUNT);
#endif

    if (s->encoding) {
        /* release non-reference frames */
        for (i = 0; i < s->picture_count; i++) {
            if (s->picture[i].f.data[0] && !s->picture[i].f.reference
                /* && s->picture[i].type != FF_BUFFER_TYPE_SHARED */) {
                free_frame_buffer(s, &s->picture[i]);
            }
        }
    }
    // clear copies, to avoid confusion
#if 0
    memset(&s->last_picture,    0, sizeof(Picture));
    memset(&s->next_picture,    0, sizeof(Picture));
    memset(&s->current_picture, 0, sizeof(Picture));
#endif
    s->avctx->coded_frame = &s->current_picture_ptr->f;

    if (s->codec_id != AV_CODEC_ID_H264 && s->current_picture.f.reference) {
        ff_thread_report_progress(&s->current_picture_ptr->f, INT_MAX, 0);
    }
}

/**
 * Initialize the PutBitContext s.
 *
 * @param buffer the buffer where to put bits
 * @param buffer_size the size in bytes of buffer
 */
static inline void init_put_bits(PutBitContext *s, uint8_t *buffer,
                                 int buffer_size)
{
	LOGD("init_put_bits buffer_size:%d",buffer_size);
	LOGD("buffer_size:%d",buffer_size);
    if (buffer_size < 0) {
        buffer_size = 0;
        buffer      = NULL;
    }

    s->size_in_bits = 8 * buffer_size;
    s->buf          = buffer;
    s->buf_end      = s->buf + buffer_size;
    s->buf_ptr      = s->buf;
    s->bit_left     = 32;
    s->bit_buf      = 0;

//	LOGD("init_put_bits Called buffer_size:%d\n",buffer_size);
}

/**
 * Write up to 31 bits into a bitstream.
 * Use put_bits32 to write 32 bits.
 */
static inline void put_bits(PutBitContext *s, int n, unsigned int value)
{
//	LOGD("put_bits n:%d val:%d\n",n,value);
    unsigned int bit_buf;
    int bit_left;

//    av_assert2(n <= 31 && value < (1U << n));

    bit_buf  = s->bit_buf;
    bit_left = s->bit_left;

    /* XXX: optimize */
#ifdef BITSTREAM_WRITER_LE
    bit_buf |= value << (32 - bit_left);
    if (n >= bit_left) {
//        av_assert2(s->buf_ptr+3<s->buf_end);
        AV_WL32(s->buf_ptr, bit_buf);
        s->buf_ptr += 4;
        bit_buf     = (bit_left == 32) ? 0 : value >> bit_left;
        bit_left   += 32;
    }
    bit_left -= n;
#else
    if (n < bit_left) {
        bit_buf     = (bit_buf << n) | value;
        bit_left   -= n;
    } else {
        bit_buf   <<= bit_left;
        bit_buf    |= value >> (n - bit_left);
//        av_assert2(s->buf_ptr+3<s->buf_end);
        AV_WB32(s->buf_ptr, bit_buf);
        s->buf_ptr += 4;
        bit_left   += 32 - n;
        bit_buf     = value;
    }
#endif

    s->bit_buf  = bit_buf;
    s->bit_left = bit_left;
}


/**
 * Write up to 31 bits into a bitstream.
 * Use put_bits32 to write 32 bits.
 */
static inline void put_bits_(PutBitContext *s, int n, unsigned int value)
{
	LOGD("put_bits_ n:%d val:%d\n",n,value);
    unsigned int bit_buf;
    int bit_left;

    bit_buf  = s->bit_buf;
    bit_left = s->bit_left;

	LOGD("bit_buf A n:%d bit_left:%d\n",bit_buf,bit_left);
    /* XXX: optimize */
#ifdef BITSTREAM_WRITER_LE
    bit_buf |= value << (32 - bit_left);
    if (n >= bit_left) {
//        av_assert2(s->buf_ptr+3<s->buf_end);
        AV_WL32(s->buf_ptr, bit_buf);
        s->buf_ptr += 4;
        bit_buf     = (bit_left == 32) ? 0 : value >> bit_left;
        bit_left   += 32;
    }
    bit_left -= n;
#else
    if (n < bit_left) {
        bit_buf     = (bit_buf << n) | value;
        bit_left   -= n;
    } else {
        bit_buf   <<= bit_left;
        bit_buf    |= value >> (n - bit_left);
//        av_assert2(s->buf_ptr+3<s->buf_end);
        AV_WB32(s->buf_ptr, bit_buf);
        s->buf_ptr += 4;
        bit_left   += 32 - n;
        bit_buf     = value;
    }
#endif

	LOGD("bit_buf B n:%d bit_left:%d\n",bit_buf,bit_left);
    s->bit_buf  = bit_buf;
    s->bit_left = bit_left;
}
static inline void put_marker(PutBitContext *p, int code)
{
    put_bits(p, 8, 0xff);
    put_bits(p, 8, code);
}

void ff_mjpeg_encode_picture_trailer(MpegEncContext *s)
{

//    av_assert1((s->header_bits&7)==0);


    put_marker(&s->pb, EOI);
}


int ff_thread_can_start_frame(AVCodecContext *avctx)
{
    PerThreadContext *p = avctx->thread_opaque;
    if ((avctx->active_thread_type&FF_THREAD_FRAME) && p->state != STATE_SETTING_UP &&
        (avctx->codec->update_thread_context || (!avctx->thread_safe_callbacks &&
                avctx->get_buffer != avcodec_default_get_buffer))) {
        return 0;
    }
    return 1;
}



void ff_release_unused_pictures(MpegEncContext*s, int remove_current)
{
    int i;

    /* release non reference frames */
    for (i = 0; i < s->picture_count; i++) {
        if (s->picture[i].f.data[0] && !s->picture[i].f.reference &&
            (!s->picture[i].owner2 || s->picture[i].owner2 == s) &&
            (remove_current || &s->picture[i] !=  s->current_picture_ptr)
            /* && s->picture[i].type!= FF_BUFFER_TYPE_SHARED */) {
            free_frame_buffer(s, &s->picture[i]);
        }
    }
}

static void update_noise_reduction(MpegEncContext *s)
{
    int intra, i;

    for (intra = 0; intra < 2; intra++) {
        if (s->dct_count[intra] > (1 << 16)) {
            for (i = 0; i < 64; i++) {
                s->dct_error_sum[intra][i] >>= 1;
            }
            s->dct_count[intra] >>= 1;
        }

        for (i = 0; i < 64; i++) {
            s->dct_offset[intra][i] = (s->avctx->noise_reduction *
                                       s->dct_count[intra] +
                                       s->dct_error_sum[intra][i] / 2) /
                                      (s->dct_error_sum[intra][i] + 1);
        }
    }
}

/**
 * generic function for encode/decode called after coding/decoding
 * the header and before a frame is coded/decoded.
 */
int ff_MPV_frame_start(MpegEncContext *s, AVCodecContext *avctx)
{
	LOGD("ff_MPV_frame_start Called\n");
    int i;
    Picture *pic;
    s->mb_skipped = 0;

    if (!ff_thread_can_start_frame(avctx)) {
        LOGD( "Attempt to start a frame outside SETUP state\n");
        return -1;
    }

    /* mark & release old frames */
    if (s->out_format != FMT_H264 || s->codec_id == AV_CODEC_ID_SVQ3) {
        if (s->pict_type != AV_PICTURE_TYPE_B && s->last_picture_ptr &&
            s->last_picture_ptr != s->next_picture_ptr &&
            s->last_picture_ptr->f.data[0]) {
            if (s->last_picture_ptr->owner2 == s)
                free_frame_buffer(s, s->last_picture_ptr);
        }

        /* release forgotten pictures */
        /* if (mpeg124/h263) */
        if (!s->encoding) {
            for (i = 0; i < s->picture_count; i++) {
                if (s->picture[i].owner2 == s && s->picture[i].f.data[0] &&
                    &s->picture[i] != s->last_picture_ptr &&
                    &s->picture[i] != s->next_picture_ptr &&
                    s->picture[i].f.reference && !s->picture[i].needs_realloc) {
                    if (!(avctx->active_thread_type & FF_THREAD_FRAME))
                    	 printf("releasing zombie picture\n");
                    free_frame_buffer(s, &s->picture[i]);
                }
            }
        }
    }

    if (!s->encoding) {
        ff_release_unused_pictures(s, 1);

        if (s->current_picture_ptr &&
            s->current_picture_ptr->f.data[0] == NULL) {
            // we already have a unused image
            // (maybe it was set before reading the header)
            pic = s->current_picture_ptr;
        } else {
            i   = ff_find_unused_picture(s, 0);
            if (i < 0) {
            	 printf( "no frame buffer available\n");
                return i;
            }
            pic = &s->picture[i];
        }

        pic->f.reference = 0;
        if (!s->droppable) {
            if (s->codec_id == AV_CODEC_ID_H264)
                pic->f.reference = s->picture_structure;
            else if (s->pict_type != AV_PICTURE_TYPE_B)
                pic->f.reference = 3;
        }

        pic->f.coded_picture_number = s->coded_picture_number++;

        if (ff_alloc_picture(s, pic, 0) < 0)
            return -1;

        s->current_picture_ptr = pic;
        // FIXME use only the vars from current_pic
        s->current_picture_ptr->f.top_field_first = s->top_field_first;
        if (s->codec_id == AV_CODEC_ID_MPEG1VIDEO ||
            s->codec_id == AV_CODEC_ID_MPEG2VIDEO) {
            if (s->picture_structure != PICT_FRAME)
                s->current_picture_ptr->f.top_field_first =
                    (s->picture_structure == PICT_TOP_FIELD) == s->first_field;
        }
        s->current_picture_ptr->f.interlaced_frame = !s->progressive_frame &&
                                                     !s->progressive_sequence;
        s->current_picture_ptr->field_picture      =  s->picture_structure != PICT_FRAME;
    }

    s->current_picture_ptr->f.pict_type = s->pict_type;
    // if (s->flags && CODEC_FLAG_QSCALE)
    //     s->current_picture_ptr->quality = s->new_picture_ptr->quality;
    s->current_picture_ptr->f.key_frame = s->pict_type == AV_PICTURE_TYPE_I;

    ff_copy_picture(&s->current_picture, s->current_picture_ptr);

    if (s->pict_type != AV_PICTURE_TYPE_B) {
        s->last_picture_ptr = s->next_picture_ptr;
        if (!s->droppable)
            s->next_picture_ptr = s->current_picture_ptr;
    }
//    LOGD("ff_MPV_frame_start L%p N%p C%p L%p N%p C%p type:%d drop:%d\n",
//            s->last_picture_ptr, s->next_picture_ptr,s->current_picture_ptr,
//            s->last_picture_ptr    ? s->last_picture_ptr->f.data[0]    : NULL,
//            s->next_picture_ptr    ? s->next_picture_ptr->f.data[0]    : NULL,
//            s->current_picture_ptr ? s->current_picture_ptr->f.data[0] : NULL,
//            s->pict_type, s->droppable);

    if (s->codec_id != AV_CODEC_ID_H264) {
        if ((s->last_picture_ptr == NULL ||
             s->last_picture_ptr->f.data[0] == NULL) &&
            (s->pict_type != AV_PICTURE_TYPE_I ||
             s->picture_structure != PICT_FRAME)) {
            int h_chroma_shift, v_chroma_shift;
            av_pix_fmt_get_chroma_sub_sample(s->avctx->pix_fmt,
                                             &h_chroma_shift, &v_chroma_shift);
            if (s->pict_type != AV_PICTURE_TYPE_I)
            	 printf("warning: first frame is no keyframe\n");
            else if (s->picture_structure != PICT_FRAME)
            	 printf( "allocate dummy last picture for field based first keyframe\n");

            /* Allocate a dummy frame */
            i = ff_find_unused_picture(s, 0);
            if (i < 0) {
            	 printf("no frame buffer available\n");
                return i;
            }
            s->last_picture_ptr = &s->picture[i];
            s->last_picture_ptr->f.key_frame = 0;
            if (ff_alloc_picture(s, s->last_picture_ptr, 0) < 0) {
                s->last_picture_ptr = NULL;
                return -1;
            }

            memset(s->last_picture_ptr->f.data[0], 0x80,
                   avctx->height * s->last_picture_ptr->f.linesize[0]);
            memset(s->last_picture_ptr->f.data[1], 0x80,
                   (avctx->height >> v_chroma_shift) *
                   s->last_picture_ptr->f.linesize[1]);
            memset(s->last_picture_ptr->f.data[2], 0x80,
                   (avctx->height >> v_chroma_shift) *
                   s->last_picture_ptr->f.linesize[2]);

            if(s->codec_id == AV_CODEC_ID_FLV1 || s->codec_id == AV_CODEC_ID_H263){
                for(i=0; i<avctx->height; i++)
                    memset(s->last_picture_ptr->f.data[0] + s->last_picture_ptr->f.linesize[0]*i, 16, avctx->width);
            }

            ff_thread_report_progress(&s->last_picture_ptr->f, INT_MAX, 0);
            ff_thread_report_progress(&s->last_picture_ptr->f, INT_MAX, 1);
            s->last_picture_ptr->f.reference = 3;
        }
        if ((s->next_picture_ptr == NULL ||
             s->next_picture_ptr->f.data[0] == NULL) &&
            s->pict_type == AV_PICTURE_TYPE_B) {
            /* Allocate a dummy frame */
            i = ff_find_unused_picture(s, 0);
            if (i < 0) {
            	 printf( "no frame buffer available\n");
                return i;
            }
            s->next_picture_ptr = &s->picture[i];
            s->next_picture_ptr->f.key_frame = 0;
            if (ff_alloc_picture(s, s->next_picture_ptr, 0) < 0) {
                s->next_picture_ptr = NULL;
                return -1;
            }
            ff_thread_report_progress(&s->next_picture_ptr->f, INT_MAX, 0);
            ff_thread_report_progress(&s->next_picture_ptr->f, INT_MAX, 1);
            s->next_picture_ptr->f.reference = 3;
        }
    }

    memset(s->last_picture.f.data, 0, sizeof(s->last_picture.f.data));
    memset(s->next_picture.f.data, 0, sizeof(s->next_picture.f.data));
    if (s->last_picture_ptr)
        ff_copy_picture(&s->last_picture, s->last_picture_ptr);
    if (s->next_picture_ptr)
        ff_copy_picture(&s->next_picture, s->next_picture_ptr);

    if (HAVE_THREADS && (avctx->active_thread_type & FF_THREAD_FRAME)) {
        if (s->next_picture_ptr)
            s->next_picture_ptr->owner2 = s;
        if (s->last_picture_ptr)
            s->last_picture_ptr->owner2 = s;
    }

//    assert(s->pict_type == AV_PICTURE_TYPE_I || (s->last_picture_ptr &&
//                                                 s->last_picture_ptr->f.data[0]));

    if (s->picture_structure!= PICT_FRAME && s->out_format != FMT_H264) {
        int i;
        for (i = 0; i < 4; i++) {
            if (s->picture_structure == PICT_BOTTOM_FIELD) {
                s->current_picture.f.data[i] +=
                    s->current_picture.f.linesize[i];
            }
            s->current_picture.f.linesize[i] *= 2;
            s->last_picture.f.linesize[i]    *= 2;
            s->next_picture.f.linesize[i]    *= 2;
        }
    }

    s->err_recognition = avctx->err_recognition;

    /* set dequantizer, we can't do it during init as
     * it might change for mpeg4 and we can't do it in the header
     * decode as init is not called for mpeg4 there yet */
    if (s->mpeg_quant || s->codec_id == AV_CODEC_ID_MPEG2VIDEO) {
        s->dct_unquantize_intra = s->dct_unquantize_mpeg2_intra;
        s->dct_unquantize_inter = s->dct_unquantize_mpeg2_inter;
    } else if (s->out_format == FMT_H263 || s->out_format == FMT_H261) {
        s->dct_unquantize_intra = s->dct_unquantize_h263_intra;
        s->dct_unquantize_inter = s->dct_unquantize_h263_inter;
    } else {
        s->dct_unquantize_intra = s->dct_unquantize_mpeg1_intra;
        s->dct_unquantize_inter = s->dct_unquantize_mpeg1_inter;
    }

    if (s->dct_error_sum) {
//        assert(s->avctx->noise_reduction && s->encoding);
        update_noise_reduction(s);
    }

    if (CONFIG_MPEG_XVMC_DECODER && s->avctx->xvmc_acceleration)
        return ff_xvmc_field_start(s, avctx);

    return 0;
}


static inline void init_ref(MotionEstContext *c, uint8_t *src[3], uint8_t *ref[3], uint8_t *ref2[3], int x, int y, int ref_index){
    const int offset[3]= {
          y*c->  stride + x,
        ((y*c->uvstride + x)>>1),
        ((y*c->uvstride + x)>>1),
    };
    int i;
    for(i=0; i<3; i++){
        c->src[0][i]= src [i] + offset[i];
        c->ref[0][i]= ref [i] + offset[i];
    }
    if(ref_index){
        for(i=0; i<3; i++){
            c->ref[ref_index][i]= ref2[i] + offset[i];
        }
    }
}


static inline int get_penalty_factor(int lambda, int lambda2, int type){
    switch(type&0xFF){
    default:
    case FF_CMP_SAD:
        return lambda>>FF_LAMBDA_SHIFT;
    case FF_CMP_DCT:
        return (3*lambda)>>(FF_LAMBDA_SHIFT+1);
    case FF_CMP_W53:
        return (4*lambda)>>(FF_LAMBDA_SHIFT);
    case FF_CMP_W97:
        return (2*lambda)>>(FF_LAMBDA_SHIFT);
    case FF_CMP_SATD:
    case FF_CMP_DCT264:
        return (2*lambda)>>FF_LAMBDA_SHIFT;
    case FF_CMP_RD:
    case FF_CMP_PSNR:
    case FF_CMP_SSE:
    case FF_CMP_NSSE:
        return lambda2>>FF_LAMBDA_SHIFT;
    case FF_CMP_BIT:
        return 1;
    }
}

/**
 * get fullpel ME search limits.
 */
static inline void get_limits(MpegEncContext *s, int x, int y)
{
    MotionEstContext * const c= &s->me;
    int range= c->avctx->me_range >> (1 + !!(c->flags&FLAG_QPEL));
    int max_range = MAX_MV >> (1 + !!(c->flags&FLAG_QPEL));
/*
    if(c->avctx->me_range) c->range= c->avctx->me_range >> 1;
    else                   c->range= 16;
*/
    if (s->unrestricted_mv) {
        c->xmin = - x - 16;
        c->ymin = - y - 16;
        c->xmax = - x + s->width;
        c->ymax = - y + s->height;
    } else if (s->out_format == FMT_H261){
        // Search range of H261 is different from other codec standards
        c->xmin = (x > 15) ? - 15 : 0;
        c->ymin = (y > 15) ? - 15 : 0;
        c->xmax = (x < s->mb_width * 16 - 16) ? 15 : 0;
        c->ymax = (y < s->mb_height * 16 - 16) ? 15 : 0;
    } else {
        c->xmin = - x;
        c->ymin = - y;
        c->xmax = - x + s->mb_width *16 - 16;
        c->ymax = - y + s->mb_height*16 - 16;
    }
    if(!range || range > max_range)
        range = max_range;
    if(range){
        c->xmin = FFMAX(c->xmin,-range);
        c->xmax = FFMIN(c->xmax, range);
        c->ymin = FFMAX(c->ymin,-range);
        c->ymax = FFMIN(c->ymax, range);
    }
}


static inline unsigned update_map_generation(MotionEstContext *c)
{
    c->map_generation+= 1<<(ME_MAP_MV_BITS*2);
    if(c->map_generation==0){
        c->map_generation= 1<<(ME_MAP_MV_BITS*2);
        memset(c->map, 0, sizeof(uint32_t)*ME_MAP_SIZE);
    }
    return c->map_generation;
}


static inline int cmp_direct_inline(MpegEncContext *s, const int x, const int y, const int subx, const int suby,
                      const int size, const int h, int ref_index, int src_index,
                      me_cmp_func cmp_func, me_cmp_func chroma_cmp_func, int qpel){
    MotionEstContext * const c= &s->me;
    const int stride= c->stride;
    const int hx= subx + (x<<(1+qpel));
    const int hy= suby + (y<<(1+qpel));
    uint8_t * const * const ref= c->ref[ref_index];
    uint8_t * const * const src= c->src[src_index];
    int d;
    //FIXME check chroma 4mv, (no crashes ...)
//        av_assert2(x >= c->xmin && hx <= c->xmax<<(qpel+1) && y >= c->ymin && hy <= c->ymax<<(qpel+1));
        if(x >= c->xmin && hx <= c->xmax<<(qpel+1) && y >= c->ymin && hy <= c->ymax<<(qpel+1)){
            const int time_pp= s->pp_time;
            const int time_pb= s->pb_time;
            const int mask= 2*qpel+1;
            if(s->mv_type==MV_TYPE_8X8){
                int i;
                for(i=0; i<4; i++){
                    int fx = c->direct_basis_mv[i][0] + hx;
                    int fy = c->direct_basis_mv[i][1] + hy;
                    int bx = hx ? fx - c->co_located_mv[i][0] : c->co_located_mv[i][0]*(time_pb - time_pp)/time_pp + ((i &1)<<(qpel+4));
                    int by = hy ? fy - c->co_located_mv[i][1] : c->co_located_mv[i][1]*(time_pb - time_pp)/time_pp + ((i>>1)<<(qpel+4));
                    int fxy= (fx&mask) + ((fy&mask)<<(qpel+1));
                    int bxy= (bx&mask) + ((by&mask)<<(qpel+1));

                    uint8_t *dst= c->temp + 8*(i&1) + 8*stride*(i>>1);
                    if(qpel){
                        c->qpel_put[1][fxy](dst, ref[0] + (fx>>2) + (fy>>2)*stride, stride);
                        c->qpel_avg[1][bxy](dst, ref[8] + (bx>>2) + (by>>2)*stride, stride);
                    }else{
                        c->hpel_put[1][fxy](dst, ref[0] + (fx>>1) + (fy>>1)*stride, stride, 8);
                        c->hpel_avg[1][bxy](dst, ref[8] + (bx>>1) + (by>>1)*stride, stride, 8);
                    }
                }
            }else{
                int fx = c->direct_basis_mv[0][0] + hx;
                int fy = c->direct_basis_mv[0][1] + hy;
                int bx = hx ? fx - c->co_located_mv[0][0] : (c->co_located_mv[0][0]*(time_pb - time_pp)/time_pp);
                int by = hy ? fy - c->co_located_mv[0][1] : (c->co_located_mv[0][1]*(time_pb - time_pp)/time_pp);
                int fxy= (fx&mask) + ((fy&mask)<<(qpel+1));
                int bxy= (bx&mask) + ((by&mask)<<(qpel+1));

                if(qpel){
                    c->qpel_put[1][fxy](c->temp               , ref[0] + (fx>>2) + (fy>>2)*stride               , stride);
                    c->qpel_put[1][fxy](c->temp + 8           , ref[0] + (fx>>2) + (fy>>2)*stride + 8           , stride);
                    c->qpel_put[1][fxy](c->temp     + 8*stride, ref[0] + (fx>>2) + (fy>>2)*stride     + 8*stride, stride);
                    c->qpel_put[1][fxy](c->temp + 8 + 8*stride, ref[0] + (fx>>2) + (fy>>2)*stride + 8 + 8*stride, stride);
                    c->qpel_avg[1][bxy](c->temp               , ref[8] + (bx>>2) + (by>>2)*stride               , stride);
                    c->qpel_avg[1][bxy](c->temp + 8           , ref[8] + (bx>>2) + (by>>2)*stride + 8           , stride);
                    c->qpel_avg[1][bxy](c->temp     + 8*stride, ref[8] + (bx>>2) + (by>>2)*stride     + 8*stride, stride);
                    c->qpel_avg[1][bxy](c->temp + 8 + 8*stride, ref[8] + (bx>>2) + (by>>2)*stride + 8 + 8*stride, stride);
                }else{
//                    av_assert2((fx>>1) + 16*s->mb_x >= -16);
//                    av_assert2((fy>>1) + 16*s->mb_y >= -16);
//                    av_assert2((fx>>1) + 16*s->mb_x <= s->width);
//                    av_assert2((fy>>1) + 16*s->mb_y <= s->height);
//                    av_assert2((bx>>1) + 16*s->mb_x >= -16);
//                    av_assert2((by>>1) + 16*s->mb_y >= -16);
//                    av_assert2((bx>>1) + 16*s->mb_x <= s->width);
//                    av_assert2((by>>1) + 16*s->mb_y <= s->height);

                    c->hpel_put[0][fxy](c->temp, ref[0] + (fx>>1) + (fy>>1)*stride, stride, 16);
                    c->hpel_avg[0][bxy](c->temp, ref[8] + (bx>>1) + (by>>1)*stride, stride, 16);
                }
            }
            d = cmp_func(s, c->temp, src[0], stride, 16);
        }else
            d= 256*256*256*32;
    return d;
}


static inline int cmp_inline(MpegEncContext *s, const int x, const int y, const int subx, const int suby,
                      const int size, const int h, int ref_index, int src_index,
                      me_cmp_func cmp_func, me_cmp_func chroma_cmp_func, int qpel, int chroma){
//   	LOGD("cmp_inline Called\n");
    MotionEstContext * const c= &s->me;
    const int stride= c->stride;
    const int uvstride= c->uvstride;
    const int dxy= subx + (suby<<(1+qpel)); //FIXME log2_subpel?
    const int hx= subx + (x<<(1+qpel));
    const int hy= suby + (y<<(1+qpel));
    uint8_t * const * const ref= c->ref[ref_index];
    uint8_t * const * const src= c->src[src_index];
    int d;
    //FIXME check chroma 4mv, (no crashes ...)
        int uvdxy;              /* no, it might not be used uninitialized */
        if(dxy){
            if(qpel){
                c->qpel_put[size][dxy](c->temp, ref[0] + x + y*stride, stride); //FIXME prototype (add h)
                if(chroma){
                    int cx= hx/2;
                    int cy= hy/2;
                    cx= (cx>>1)|(cx&1);
                    cy= (cy>>1)|(cy&1);
                    uvdxy= (cx&1) + 2*(cy&1);
                    //FIXME x/y wrong, but mpeg4 qpel is sick anyway, we should drop as much of it as possible in favor for h264
                }
            }else{
                c->hpel_put[size][dxy](c->temp, ref[0] + x + y*stride, stride, h);
                if(chroma)
                    uvdxy= dxy | (x&1) | (2*(y&1));
            }
            d = cmp_func(s, c->temp, src[0], stride, h);
        }else{
//           	LOGD("cmp_inline x:%d y:%d strid:%d h:%d\n",x,y,stride,h);
            d = cmp_func(s, src[0], ref[0] + x + y*stride, stride, h);
            if(chroma)
                uvdxy= (x&1) + 2*(y&1);
        }
        if(chroma){
            uint8_t * const uvtemp= c->temp + 16*stride;
            c->hpel_put[size+1][uvdxy](uvtemp  , ref[1] + (x>>1) + (y>>1)*uvstride, uvstride, h>>1);
            c->hpel_put[size+1][uvdxy](uvtemp+8, ref[2] + (x>>1) + (y>>1)*uvstride, uvstride, h>>1);
            d += chroma_cmp_func(s, uvtemp  , src[1], uvstride, h>>1);
            d += chroma_cmp_func(s, uvtemp+8, src[2], uvstride, h>>1);
        }
    return d;
}

static int cmp_internal(MpegEncContext *s, const int x, const int y, const int subx, const int suby,
                      const int size, const int h, int ref_index, int src_index,
                      me_cmp_func cmp_func, me_cmp_func chroma_cmp_func, const int flags){
//   	LOGD("cmp_internal Called\n");
    if(flags&FLAG_DIRECT){
        return cmp_direct_inline(s,x,y,subx,suby,size,h,ref_index,src_index, cmp_func, chroma_cmp_func, flags&FLAG_QPEL);
    }else{
        return cmp_inline(s,x,y,subx,suby,size,h,ref_index,src_index, cmp_func, chroma_cmp_func, flags&FLAG_QPEL, flags&FLAG_CHROMA);
    }
}

/** @brief compares a block (either a full macroblock or a partition thereof)
    against a proposed motion-compensated prediction of that block
 */
static inline int cmp(MpegEncContext *s, const int x, const int y, const int subx, const int suby,
                      const int size, const int h, int ref_index, int src_index,
                      me_cmp_func cmp_func, me_cmp_func chroma_cmp_func, const int flags){
//   	LOGD("cmp Called\n");
    if(av_builtin_constant_p(flags) && av_builtin_constant_p(h) && av_builtin_constant_p(size)
       && av_builtin_constant_p(subx) && av_builtin_constant_p(suby)
       && flags==0 && h==16 && size==0 && subx==0 && suby==0){
        return cmp_simple(s,x,y,ref_index,src_index, cmp_func, chroma_cmp_func);
    }else if(av_builtin_constant_p(subx) && av_builtin_constant_p(suby)
       && subx==0 && suby==0){
        return cmp_fpel_internal(s,x,y,size,h,ref_index,src_index, cmp_func, chroma_cmp_func,flags);
    }else{
        return cmp_internal(s,x,y,subx,suby,size,h,ref_index,src_index, cmp_func, chroma_cmp_func, flags);
    }
}

static int funny_diamond_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags)
{
    MotionEstContext * const c= &s->me;
    me_cmp_func cmpf, chroma_cmpf;
    int dia_size;
    LOAD_COMMON
    LOAD_COMMON2
    unsigned map_generation = c->map_generation;

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    for(dia_size=1; dia_size<=4; dia_size++){
        int dir;
        const int x= best[0];
        const int y= best[1];

        if(dia_size&(dia_size-1)) continue;

        if(   x + dia_size > xmax
           || x - dia_size < xmin
           || y + dia_size > ymax
           || y - dia_size < ymin)
           continue;

        for(dir= 0; dir<dia_size; dir+=2){
            int d;

            CHECK_MV(x + dir           , y + dia_size - dir);
            CHECK_MV(x + dia_size - dir, y - dir           );
            CHECK_MV(x - dir           , y - dia_size + dir);
            CHECK_MV(x - dia_size + dir, y + dir           );
        }

        if(x!=best[0] || y!=best[1])
            dia_size=0;
    }
    return dmin;
}

static int minima_cmp(const void *a, const void *b){
    const Minima *da = (const Minima *) a;
    const Minima *db = (const Minima *) b;

    return da->height - db->height;
}

static int sab_diamond_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags)
{
    MotionEstContext * const c= &s->me;
    me_cmp_func cmpf, chroma_cmpf;
    Minima minima[MAX_SAB_SIZE];
    const int minima_count= FFABS(c->dia_size);
    int i, j;
    LOAD_COMMON
    LOAD_COMMON2
    unsigned map_generation = c->map_generation;

//    av_assert1(minima_count <= MAX_SAB_SIZE);

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    /*Note j<MAX_SAB_SIZE is needed if MAX_SAB_SIZE < ME_MAP_SIZE as j can
      become larger due to MVs overflowing their ME_MAP_MV_BITS bits space in map
     */
    for(j=i=0; i<ME_MAP_SIZE && j<MAX_SAB_SIZE; i++){
        uint32_t key= map[i];

        key += (1<<(ME_MAP_MV_BITS-1)) + (1<<(2*ME_MAP_MV_BITS-1));

        if((key&((-1)<<(2*ME_MAP_MV_BITS))) != map_generation) continue;

        minima[j].height= score_map[i];
        minima[j].x= key & ((1<<ME_MAP_MV_BITS)-1); key>>=ME_MAP_MV_BITS;
        minima[j].y= key & ((1<<ME_MAP_MV_BITS)-1);
        minima[j].x-= (1<<(ME_MAP_MV_BITS-1));
        minima[j].y-= (1<<(ME_MAP_MV_BITS-1));

        // all entries in map should be in range except if the mv overflows their ME_MAP_MV_BITS bits space
        if(   minima[j].x > xmax || minima[j].x < xmin
           || minima[j].y > ymax || minima[j].y < ymin)
            continue;

        minima[j].checked=0;
        if(minima[j].x || minima[j].y)
            minima[j].height+= (mv_penalty[((minima[j].x)<<shift)-pred_x] + mv_penalty[((minima[j].y)<<shift)-pred_y])*penalty_factor;

        j++;
    }

    qsort(minima, j, sizeof(Minima), minima_cmp);

    for(; j<minima_count; j++){
        minima[j].height=256*256*256*64;
        minima[j].checked=0;
        minima[j].x= minima[j].y=0;
    }

    for(i=0; i<minima_count; i++){
        const int x= minima[i].x;
        const int y= minima[i].y;
        int d;

        if(minima[i].checked) continue;

        if(   x >= xmax || x <= xmin
           || y >= ymax || y <= ymin)
           continue;

        SAB_CHECK_MV(x-1, y)
        SAB_CHECK_MV(x+1, y)
        SAB_CHECK_MV(x  , y-1)
        SAB_CHECK_MV(x  , y+1)

        minima[i].checked= 1;
    }

    best[0]= minima[0].x;
    best[1]= minima[0].y;
    dmin= minima[0].height;

    if(   best[0] < xmax && best[0] > xmin
       && best[1] < ymax && best[1] > ymin){
        int d;
        //ensure that the refernece samples for hpel refinement are in the map
        CHECK_MV(best[0]-1, best[1])
        CHECK_MV(best[0]+1, best[1])
        CHECK_MV(best[0], best[1]-1)
        CHECK_MV(best[0], best[1]+1)
    }
    return dmin;
}


static inline int small_diamond_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags)
{
    MotionEstContext * const c= &s->me;
    me_cmp_func cmpf, chroma_cmpf;
    int next_dir=-1;
    LOAD_COMMON
    LOAD_COMMON2
    unsigned map_generation = c->map_generation;

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    { /* ensure that the best point is in the MAP as h/qpel refinement needs it */
        const unsigned key = (best[1]<<ME_MAP_MV_BITS) + best[0] + map_generation;
        const int index= ((best[1]<<ME_MAP_SHIFT) + best[0])&(ME_MAP_SIZE-1);
        if(map[index]!=key){ //this will be executed only very rarey
            score_map[index]= cmp(s, best[0], best[1], 0, 0, size, h, ref_index, src_index, cmpf, chroma_cmpf, flags);
            map[index]= key;
        }
    }

    for(;;){
        int d;
        const int dir= next_dir;
        const int x= best[0];
        const int y= best[1];
        next_dir=-1;

        if(dir!=2 && x>xmin) CHECK_MV_DIR(x-1, y  , 0)
        if(dir!=3 && y>ymin) CHECK_MV_DIR(x  , y-1, 1)
        if(dir!=0 && x<xmax) CHECK_MV_DIR(x+1, y  , 2)
        if(dir!=1 && y<ymax) CHECK_MV_DIR(x  , y+1, 3)

        if(next_dir==-1){
            return dmin;
        }
    }
    return 0;
}



static int full_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags)
{
    MotionEstContext * const c= &s->me;
    me_cmp_func cmpf, chroma_cmpf;
    LOAD_COMMON
    LOAD_COMMON2
    unsigned map_generation = c->map_generation;
    int x,y, d;
    const int dia_size= c->dia_size&0xFF;

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    for(y=FFMAX(-dia_size, ymin); y<=FFMIN(dia_size,ymax); y++){
        for(x=FFMAX(-dia_size, xmin); x<=FFMIN(dia_size,xmax); x++){
            CHECK_MV(x, y);
        }
    }

    x= best[0];
    y= best[1];
    d= dmin;
    CHECK_CLIPPED_MV(x  , y);
    CHECK_CLIPPED_MV(x+1, y);
    CHECK_CLIPPED_MV(x, y+1);
    CHECK_CLIPPED_MV(x-1, y);
    CHECK_CLIPPED_MV(x, y-1);
    best[0]= x;
    best[1]= y;

    return d;
}

int hex_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags, int dia_size)
{
    MotionEstContext * const c= &s->me;
    me_cmp_func cmpf, chroma_cmpf;
    LOAD_COMMON
    LOAD_COMMON2
    unsigned map_generation = c->map_generation;
    int x,y,d;
    const int dec= dia_size & (dia_size-1);

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    for(;dia_size; dia_size= dec ? dia_size-1 : dia_size>>1){
        do{
            x= best[0];
            y= best[1];

            CHECK_CLIPPED_MV(x  -dia_size    , y);
            CHECK_CLIPPED_MV(x+  dia_size    , y);
            CHECK_CLIPPED_MV(x+( dia_size>>1), y+dia_size);
            CHECK_CLIPPED_MV(x+( dia_size>>1), y-dia_size);
            if(dia_size>1){
                CHECK_CLIPPED_MV(x+(-dia_size>>1), y+dia_size);
                CHECK_CLIPPED_MV(x+(-dia_size>>1), y-dia_size);
            }
        }while(best[0] != x || best[1] != y);
    }

    return dmin;
}

static int umh_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags)
{
    MotionEstContext * const c= &s->me;
    me_cmp_func cmpf, chroma_cmpf;
    LOAD_COMMON
    LOAD_COMMON2
    unsigned map_generation = c->map_generation;
    int x,y,x2,y2, i, j, d;
    const int dia_size= c->dia_size&0xFE;
    static const int hex[16][2]={{-4,-2}, {-4,-1}, {-4, 0}, {-4, 1}, {-4, 2},
                                 { 4,-2}, { 4,-1}, { 4, 0}, { 4, 1}, { 4, 2},
                                 {-2, 3}, { 0, 4}, { 2, 3},
                                 {-2,-3}, { 0,-4}, { 2,-3},};

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    x= best[0];
    y= best[1];
    for(x2=FFMAX(x-dia_size+1, xmin); x2<=FFMIN(x+dia_size-1,xmax); x2+=2){
        CHECK_MV(x2, y);
    }
    for(y2=FFMAX(y-dia_size/2+1, ymin); y2<=FFMIN(y+dia_size/2-1,ymax); y2+=2){
        CHECK_MV(x, y2);
    }

    x= best[0];
    y= best[1];
    for(y2=FFMAX(y-2, ymin); y2<=FFMIN(y+2,ymax); y2++){
        for(x2=FFMAX(x-2, xmin); x2<=FFMIN(x+2,xmax); x2++){
            CHECK_MV(x2, y2);
        }
    }

//FIXME prevent the CLIP stuff

    for(j=1; j<=dia_size/4; j++){
        for(i=0; i<16; i++){
            CHECK_CLIPPED_MV(x+hex[i][0]*j, y+hex[i][1]*j);
        }
    }

    return hex_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags, 2);
}




static int l2s_dia_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags)
{
    MotionEstContext * const c= &s->me;
    me_cmp_func cmpf, chroma_cmpf;
    LOAD_COMMON
    LOAD_COMMON2
    unsigned map_generation = c->map_generation;
    int x,y,i,d;
    int dia_size= c->dia_size&0xFF;
    const int dec= dia_size & (dia_size-1);
    static const int hex[8][2]={{-2, 0}, {-1,-1}, { 0,-2}, { 1,-1},
                                { 2, 0}, { 1, 1}, { 0, 2}, {-1, 1}};

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    for(; dia_size; dia_size= dec ? dia_size-1 : dia_size>>1){
        do{
            x= best[0];
            y= best[1];
            for(i=0; i<8; i++){
                CHECK_CLIPPED_MV(x+hex[i][0]*dia_size, y+hex[i][1]*dia_size);
            }
        }while(best[0] != x || best[1] != y);
    }

    x= best[0];
    y= best[1];
    CHECK_CLIPPED_MV(x+1, y);
    CHECK_CLIPPED_MV(x, y+1);
    CHECK_CLIPPED_MV(x-1, y);
    CHECK_CLIPPED_MV(x, y-1);

    return dmin;
}

static int var_diamond_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags)
{
    MotionEstContext * const c= &s->me;
    me_cmp_func cmpf, chroma_cmpf;
    int dia_size;
    LOAD_COMMON
    LOAD_COMMON2
    unsigned map_generation = c->map_generation;

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    for(dia_size=1; dia_size<=c->dia_size; dia_size++){
        int dir, start, end;
        const int x= best[0];
        const int y= best[1];

        start= FFMAX(0, y + dia_size - ymax);
        end  = FFMIN(dia_size, xmax - x + 1);
        for(dir= start; dir<end; dir++){
            int d;

//check(x + dir,y + dia_size - dir,0, a0)
            CHECK_MV(x + dir           , y + dia_size - dir);
        }

        start= FFMAX(0, x + dia_size - xmax);
        end  = FFMIN(dia_size, y - ymin + 1);
        for(dir= start; dir<end; dir++){
            int d;

//check(x + dia_size - dir, y - dir,0, a1)
            CHECK_MV(x + dia_size - dir, y - dir           );
        }

        start= FFMAX(0, -y + dia_size + ymin );
        end  = FFMIN(dia_size, x - xmin + 1);
        for(dir= start; dir<end; dir++){
            int d;

//check(x - dir,y - dia_size + dir,0, a2)
            CHECK_MV(x - dir           , y - dia_size + dir);
        }

        start= FFMAX(0, -x + dia_size + xmin );
        end  = FFMIN(dia_size, ymax - y + 1);
        for(dir= start; dir<end; dir++){
            int d;

//check(x - dia_size + dir, y + dir,0, a3)
            CHECK_MV(x - dia_size + dir, y + dir           );
        }

        if(x!=best[0] || y!=best[1])
            dia_size=0;
    }
    return dmin;
}

static inline int diamond_search(MpegEncContext * s, int *best, int dmin,
                                       int src_index, int ref_index, int const penalty_factor,
                                       int size, int h, int flags){
    MotionEstContext * const c= &s->me;
    if(c->dia_size==-1)
        return funny_diamond_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);
    else if(c->dia_size<-1)
        return   sab_diamond_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);
    else if(c->dia_size<2)
        return small_diamond_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);
    else if(c->dia_size>1024)
        return          full_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);
    else if(c->dia_size>768)
        return           umh_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);
    else if(c->dia_size>512)
        return           hex_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags, c->dia_size&0xFF);
    else if(c->dia_size>256)
        return       l2s_dia_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);
    else
        return   var_diamond_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);
}


/**
   @param P a list of candidate mvs to check before starting the
   iterative search. If one of the candidates is close to the optimal mv, then
   it takes fewer iterations. And it increases the chance that we find the
   optimal mv.
 */
static inline int epzs_motion_search_internal(MpegEncContext * s, int *mx_ptr, int *my_ptr,
                             int P[10][2], int src_index, int ref_index, int16_t (*last_mv)[2],
                             int ref_mv_scale, int flags, int size, int h)
{
//   	LOGD("epzs_motion_search_internal Called\n");
    MotionEstContext * const c= &s->me;
    int best[2]={0, 0};      /**< x and y coordinates of the best motion vector.
                               i.e. the difference between the position of the
                               block currently being encoded and the position of
                               the block chosen to predict it from. */
    int d;                   ///< the score (cmp + penalty) of any given mv
    int dmin;                /**< the best value of d, i.e. the score
                               corresponding to the mv stored in best[]. */
    unsigned map_generation;
    int penalty_factor;
    const int ref_mv_stride= s->mb_stride; //pass as arg  FIXME
    const int ref_mv_xy= s->mb_x + s->mb_y*ref_mv_stride; //add to last_mv beforepassing FIXME
    me_cmp_func cmpf, chroma_cmpf;

    LOAD_COMMON
    LOAD_COMMON2

    if(c->pre_pass){
        penalty_factor= c->pre_penalty_factor;
        cmpf= s->dsp.me_pre_cmp[size];
        chroma_cmpf= s->dsp.me_pre_cmp[size+1];
    }else{
        penalty_factor= c->penalty_factor;
        cmpf= s->dsp.me_cmp[size];
        chroma_cmpf= s->dsp.me_cmp[size+1];
    }

    map_generation= update_map_generation(c);

//   	LOGD("epzs_motion_search_internal cmpf:%d\n",cmpf);
//    av_assert2(cmpf);
    dmin= cmp(s, 0, 0, 0, 0, size, h, ref_index, src_index, cmpf, chroma_cmpf, flags);
    map[0]= map_generation;
    score_map[0]= dmin;

    //FIXME precalc first term below?
    if((s->pict_type == AV_PICTURE_TYPE_B && !(c->flags & FLAG_DIRECT)) || (s->flags&CODEC_FLAG_MV0))
        dmin += (mv_penalty[pred_x] + mv_penalty[pred_y])*penalty_factor;

    /* first line */
    if (s->first_slice_line) {

        CHECK_MV(P_LEFT[0]>>shift, P_LEFT[1]>>shift)
        CHECK_CLIPPED_MV((last_mv[ref_mv_xy][0]*ref_mv_scale + (1<<15))>>16,
                        (last_mv[ref_mv_xy][1]*ref_mv_scale + (1<<15))>>16)
    }else{
        if(dmin<((h*h*s->avctx->mv0_threshold)>>8)
                    && ( P_LEFT[0]    |P_LEFT[1]
                        |P_TOP[0]     |P_TOP[1]
                        |P_TOPRIGHT[0]|P_TOPRIGHT[1])==0){
            *mx_ptr= 0;
            *my_ptr= 0;
            c->skip=1;
            return dmin;
        }
        CHECK_MV(    P_MEDIAN[0] >>shift ,    P_MEDIAN[1] >>shift)
        CHECK_CLIPPED_MV((P_MEDIAN[0]>>shift)  , (P_MEDIAN[1]>>shift)-1)
        CHECK_CLIPPED_MV((P_MEDIAN[0]>>shift)  , (P_MEDIAN[1]>>shift)+1)
        CHECK_CLIPPED_MV((P_MEDIAN[0]>>shift)-1, (P_MEDIAN[1]>>shift)  )
        CHECK_CLIPPED_MV((P_MEDIAN[0]>>shift)+1, (P_MEDIAN[1]>>shift)  )
        CHECK_CLIPPED_MV((last_mv[ref_mv_xy][0]*ref_mv_scale + (1<<15))>>16,
                        (last_mv[ref_mv_xy][1]*ref_mv_scale + (1<<15))>>16)
        CHECK_MV(P_LEFT[0]    >>shift, P_LEFT[1]    >>shift)
        CHECK_MV(P_TOP[0]     >>shift, P_TOP[1]     >>shift)
        CHECK_MV(P_TOPRIGHT[0]>>shift, P_TOPRIGHT[1]>>shift)
    }
    if(dmin>h*h*4){
        if(c->pre_pass){
            CHECK_CLIPPED_MV((last_mv[ref_mv_xy-1][0]*ref_mv_scale + (1<<15))>>16,
                            (last_mv[ref_mv_xy-1][1]*ref_mv_scale + (1<<15))>>16)
            if(!s->first_slice_line)
                CHECK_CLIPPED_MV((last_mv[ref_mv_xy-ref_mv_stride][0]*ref_mv_scale + (1<<15))>>16,
                                (last_mv[ref_mv_xy-ref_mv_stride][1]*ref_mv_scale + (1<<15))>>16)
        }else{
            CHECK_CLIPPED_MV((last_mv[ref_mv_xy+1][0]*ref_mv_scale + (1<<15))>>16,
                            (last_mv[ref_mv_xy+1][1]*ref_mv_scale + (1<<15))>>16)
            if(s->mb_y+1<s->end_mb_y)  //FIXME replace at least with last_slice_line
                CHECK_CLIPPED_MV((last_mv[ref_mv_xy+ref_mv_stride][0]*ref_mv_scale + (1<<15))>>16,
                                (last_mv[ref_mv_xy+ref_mv_stride][1]*ref_mv_scale + (1<<15))>>16)
        }
    }

    if(c->avctx->last_predictor_count){
        const int count= c->avctx->last_predictor_count;
        const int xstart= FFMAX(0, s->mb_x - count);
        const int ystart= FFMAX(0, s->mb_y - count);
        const int xend= FFMIN(s->mb_width , s->mb_x + count + 1);
        const int yend= FFMIN(s->mb_height, s->mb_y + count + 1);
        int mb_y;

        for(mb_y=ystart; mb_y<yend; mb_y++){
            int mb_x;
            for(mb_x=xstart; mb_x<xend; mb_x++){
                const int xy= mb_x + 1 + (mb_y + 1)*ref_mv_stride;
                int mx= (last_mv[xy][0]*ref_mv_scale + (1<<15))>>16;
                int my= (last_mv[xy][1]*ref_mv_scale + (1<<15))>>16;

                if(mx>xmax || mx<xmin || my>ymax || my<ymin) continue;
                CHECK_MV(mx,my)
            }
        }
    }

//check(best[0],best[1],0, b0)
    dmin= diamond_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);

//check(best[0],best[1],0, b1)
    *mx_ptr= best[0];
    *my_ptr= best[1];

    return dmin;
}


//this function is dedicated to the braindamaged gcc
int ff_epzs_motion_search(MpegEncContext *s, int *mx_ptr, int *my_ptr,
                          int P[10][2], int src_index, int ref_index,
                          int16_t (*last_mv)[2], int ref_mv_scale,
                          int size, int h)
{
    MotionEstContext * const c= &s->me;
//FIXME convert other functions in the same way if faster
    if(c->flags==0 && h==16 && size==0){
        return epzs_motion_search_internal(s, mx_ptr, my_ptr, P, src_index, ref_index, last_mv, ref_mv_scale, 0, 0, 16);
//    case FLAG_QPEL://元からコメントアウト
//        return epzs_motion_search_internal(s, mx_ptr, my_ptr, P, src_index, ref_index, last_mv, ref_mv_scale, FLAG_QPEL);
    }else{
        return epzs_motion_search_internal(s, mx_ptr, my_ptr, P, src_index, ref_index, last_mv, ref_mv_scale, c->flags, size, h);
    }
}


static inline int mid_pred(int a, int b, int c)
{
#if 0
    int t= (a-b)&((a-b)>>31);
    a-=t;
    b+=t;
    b-= (b-c)&((b-c)>>31);
    b+= (a-b)&((a-b)>>31);

    return b;
#else
    if(a>b){
        if(c>b){
            if(c>a) b=a;
            else    b=c;
        }
    }else{
        if(b>c){
            if(c>a) b=c;
            else    b=a;
        }
    }
    return b;
#endif
}

int ff_pre_estimate_p_frame_motion(MpegEncContext * s,
                                    int mb_x, int mb_y)
{
//    LOGD("ff_pre_estimate_p_frame_motion Called\n");
    MotionEstContext * const c= &s->me;
    int mx, my, dmin;
    int P[10][2];
    const int shift= 1+s->quarter_sample;
    const int xy= mb_x + mb_y*s->mb_stride;
    init_ref(c, s->new_picture.f.data, s->last_picture.f.data, NULL, 16*mb_x, 16*mb_y, 0);

//    av_assert0(s->quarter_sample==0 || s->quarter_sample==1);

    c->pre_penalty_factor    = get_penalty_factor(s->lambda, s->lambda2, c->avctx->me_pre_cmp);
    c->current_mv_penalty= c->mv_penalty[s->f_code] + MAX_MV;

    get_limits(s, 16*mb_x, 16*mb_y);
    c->skip=0;

    P_LEFT[0]       = s->p_mv_table[xy + 1][0];
    P_LEFT[1]       = s->p_mv_table[xy + 1][1];

    if(P_LEFT[0]       < (c->xmin<<shift)) P_LEFT[0]       = (c->xmin<<shift);

    /* special case for first line */
    if (s->first_slice_line) {
        c->pred_x= P_LEFT[0];
        c->pred_y= P_LEFT[1];
        P_TOP[0]= P_TOPRIGHT[0]= P_MEDIAN[0]=
        P_TOP[1]= P_TOPRIGHT[1]= P_MEDIAN[1]= 0; //FIXME
    } else {
        P_TOP[0]      = s->p_mv_table[xy + s->mb_stride    ][0];
        P_TOP[1]      = s->p_mv_table[xy + s->mb_stride    ][1];
        P_TOPRIGHT[0] = s->p_mv_table[xy + s->mb_stride - 1][0];
        P_TOPRIGHT[1] = s->p_mv_table[xy + s->mb_stride - 1][1];
        if(P_TOP[1]      < (c->ymin<<shift)) P_TOP[1]     = (c->ymin<<shift);
        if(P_TOPRIGHT[0] > (c->xmax<<shift)) P_TOPRIGHT[0]= (c->xmax<<shift);
        if(P_TOPRIGHT[1] < (c->ymin<<shift)) P_TOPRIGHT[1]= (c->ymin<<shift);

        P_MEDIAN[0]= mid_pred(P_LEFT[0], P_TOP[0], P_TOPRIGHT[0]);
        P_MEDIAN[1]= mid_pred(P_LEFT[1], P_TOP[1], P_TOPRIGHT[1]);

        c->pred_x = P_MEDIAN[0];
        c->pred_y = P_MEDIAN[1];
    }

    dmin = ff_epzs_motion_search(s, &mx, &my, P, 0, 0, s->p_mv_table, (1<<16)>>shift, 0, 16);

    s->p_mv_table[xy][0] = mx<<shift;
    s->p_mv_table[xy][1] = my<<shift;

    return dmin;
}

static int pre_estimate_motion_thread(AVCodecContext *c, void *arg){
//    LOGD("pre_estimate_motion_thread Called\n");
    MpegEncContext *s= *(void**)arg;


    s->me.pre_pass=1;
    s->me.dia_size= s->avctx->pre_dia_size;
    s->first_slice_line=1;
    for(s->mb_y= s->end_mb_y-1; s->mb_y >= s->start_mb_y; s->mb_y--) {
        for(s->mb_x=s->mb_width-1; s->mb_x >=0 ;s->mb_x--) {
            ff_pre_estimate_p_frame_motion(s, s->mb_x, s->mb_y);
        }
        s->first_slice_line=0;
    }

    s->me.pre_pass=0;

    return 0;
}




static int cmp_hpel(MpegEncContext *s, const int x, const int y, const int subx, const int suby,
                      const int size, const int h, int ref_index, int src_index,
                      me_cmp_func cmp_func, me_cmp_func chroma_cmp_func, const int flags){
    if(flags&FLAG_DIRECT){
        return cmp_direct_inline(s,x,y,subx,suby,size,h,ref_index,src_index, cmp_func, chroma_cmp_func, 0);
    }else{
        return cmp_inline(s,x,y,subx,suby,size,h,ref_index,src_index, cmp_func, chroma_cmp_func, 0, flags&FLAG_CHROMA);
    }
}

static int hpel_motion_search(MpegEncContext * s,
                                  int *mx_ptr, int *my_ptr, int dmin,
                                  int src_index, int ref_index,
                                  int size, int h)
{
    MotionEstContext * const c= &s->me;
    const int mx = *mx_ptr;
    const int my = *my_ptr;
    const int penalty_factor= c->sub_penalty_factor;
    me_cmp_func cmp_sub, chroma_cmp_sub;
    int bx=2*mx, by=2*my;

    LOAD_COMMON
    int flags= c->sub_flags;

 //FIXME factorize

    cmp_sub= s->dsp.me_sub_cmp[size];
    chroma_cmp_sub= s->dsp.me_sub_cmp[size+1];

    if(c->skip){ //FIXME move out of hpel?
        *mx_ptr = 0;
        *my_ptr = 0;
        return dmin;
    }

    if(c->avctx->me_cmp != c->avctx->me_sub_cmp){
        dmin= cmp(s, mx, my, 0, 0, size, h, ref_index, src_index, cmp_sub, chroma_cmp_sub, flags);
        if(mx || my || size>0)
            dmin += (mv_penalty[2*mx - pred_x] + mv_penalty[2*my - pred_y])*penalty_factor;
    }

    if (mx > xmin && mx < xmax &&
        my > ymin && my < ymax) {
        int d= dmin;
        const int index= (my<<ME_MAP_SHIFT) + mx;
        const int t= score_map[(index-(1<<ME_MAP_SHIFT))&(ME_MAP_SIZE-1)]
                     + (mv_penalty[bx   - pred_x] + mv_penalty[by-2 - pred_y])*c->penalty_factor;
        const int l= score_map[(index- 1               )&(ME_MAP_SIZE-1)]
                     + (mv_penalty[bx-2 - pred_x] + mv_penalty[by   - pred_y])*c->penalty_factor;
        const int r= score_map[(index+ 1               )&(ME_MAP_SIZE-1)]
                     + (mv_penalty[bx+2 - pred_x] + mv_penalty[by   - pred_y])*c->penalty_factor;
        const int b= score_map[(index+(1<<ME_MAP_SHIFT))&(ME_MAP_SIZE-1)]
                     + (mv_penalty[bx   - pred_x] + mv_penalty[by+2 - pred_y])*c->penalty_factor;

#if defined(ASSERT_LEVEL) && ASSERT_LEVEL > 1
        unsigned key;
        unsigned map_generation= c->map_generation;
        key= ((my-1)<<ME_MAP_MV_BITS) + (mx) + map_generation;
//        av_assert2(c->map[(index-(1<<ME_MAP_SHIFT))&(ME_MAP_SIZE-1)] == key);
        key= ((my+1)<<ME_MAP_MV_BITS) + (mx) + map_generation;
//        av_assert2(c->map[(index+(1<<ME_MAP_SHIFT))&(ME_MAP_SIZE-1)] == key);
        key= ((my)<<ME_MAP_MV_BITS) + (mx+1) + map_generation;
//        av_assert2(c->map[(index+1)&(ME_MAP_SIZE-1)] == key);
        key= ((my)<<ME_MAP_MV_BITS) + (mx-1) + map_generation;
//        av_assert2(c->map[(index-1)&(ME_MAP_SIZE-1)] == key);
#endif
        if(t<=b){
            CHECK_HALF_MV(0, 1, mx  ,my-1)
            if(l<=r){
                CHECK_HALF_MV(1, 1, mx-1, my-1)
                if(t+r<=b+l){
                    CHECK_HALF_MV(1, 1, mx  , my-1)
                }else{
                    CHECK_HALF_MV(1, 1, mx-1, my  )
                }
                CHECK_HALF_MV(1, 0, mx-1, my  )
            }else{
                CHECK_HALF_MV(1, 1, mx  , my-1)
                if(t+l<=b+r){
                    CHECK_HALF_MV(1, 1, mx-1, my-1)
                }else{
                    CHECK_HALF_MV(1, 1, mx  , my  )
                }
                CHECK_HALF_MV(1, 0, mx  , my  )
            }
        }else{
            if(l<=r){
                if(t+l<=b+r){
                    CHECK_HALF_MV(1, 1, mx-1, my-1)
                }else{
                    CHECK_HALF_MV(1, 1, mx  , my  )
                }
                CHECK_HALF_MV(1, 0, mx-1, my)
                CHECK_HALF_MV(1, 1, mx-1, my)
            }else{
                if(t+r<=b+l){
                    CHECK_HALF_MV(1, 1, mx  , my-1)
                }else{
                    CHECK_HALF_MV(1, 1, mx-1, my)
                }
                CHECK_HALF_MV(1, 0, mx  , my)
                CHECK_HALF_MV(1, 1, mx  , my)
            }
            CHECK_HALF_MV(0, 1, mx  , my)
        }
//        av_assert2(bx >= xmin*2 && bx <= xmax*2 && by >= ymin*2 && by <= ymax*2);
    }

    *mx_ptr = bx;
    *my_ptr = by;

    return dmin;
}

static int cmp_qpel(MpegEncContext *s, const int x, const int y, const int subx, const int suby,
                      const int size, const int h, int ref_index, int src_index,
                      me_cmp_func cmp_func, me_cmp_func chroma_cmp_func, const int flags){
    if(flags&FLAG_DIRECT){
        return cmp_direct_inline(s,x,y,subx,suby,size,h,ref_index,src_index, cmp_func, chroma_cmp_func, 1);
    }else{
        return cmp_inline(s,x,y,subx,suby,size,h,ref_index,src_index, cmp_func, chroma_cmp_func, 1, flags&FLAG_CHROMA);
    }
}

static int qpel_motion_search(MpegEncContext * s,
                                  int *mx_ptr, int *my_ptr, int dmin,
                                  int src_index, int ref_index,
                                  int size, int h)
{
    MotionEstContext * const c= &s->me;
    const int mx = *mx_ptr;
    const int my = *my_ptr;
    const int penalty_factor= c->sub_penalty_factor;
    const unsigned map_generation = c->map_generation;
    const int subpel_quality= c->avctx->me_subpel_quality;
    uint32_t *map= c->map;
    me_cmp_func cmpf, chroma_cmpf;
    me_cmp_func cmp_sub, chroma_cmp_sub;

    LOAD_COMMON
    int flags= c->sub_flags;

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1]; //factorize FIXME
 //FIXME factorize

    cmp_sub= s->dsp.me_sub_cmp[size];
    chroma_cmp_sub= s->dsp.me_sub_cmp[size+1];

    if(c->skip){ //FIXME somehow move up (benchmark)
        *mx_ptr = 0;
        *my_ptr = 0;
        return dmin;
    }

    if(c->avctx->me_cmp != c->avctx->me_sub_cmp){
        dmin= cmp(s, mx, my, 0, 0, size, h, ref_index, src_index, cmp_sub, chroma_cmp_sub, flags);
        if(mx || my || size>0)
            dmin += (mv_penalty[4*mx - pred_x] + mv_penalty[4*my - pred_y])*penalty_factor;
    }

    if (mx > xmin && mx < xmax &&
        my > ymin && my < ymax) {
        int bx=4*mx, by=4*my;
        int d= dmin;
        int i, nx, ny;
        const int index= (my<<ME_MAP_SHIFT) + mx;
        const int t= score_map[(index-(1<<ME_MAP_SHIFT)  )&(ME_MAP_SIZE-1)];
        const int l= score_map[(index- 1                 )&(ME_MAP_SIZE-1)];
        const int r= score_map[(index+ 1                 )&(ME_MAP_SIZE-1)];
        const int b= score_map[(index+(1<<ME_MAP_SHIFT)  )&(ME_MAP_SIZE-1)];
        const int c= score_map[(index                    )&(ME_MAP_SIZE-1)];
        int best[8];
        int best_pos[8][2];

        memset(best, 64, sizeof(int)*8);
        if(s->me.dia_size>=2){
            const int tl= score_map[(index-(1<<ME_MAP_SHIFT)-1)&(ME_MAP_SIZE-1)];
            const int bl= score_map[(index+(1<<ME_MAP_SHIFT)-1)&(ME_MAP_SIZE-1)];
            const int tr= score_map[(index-(1<<ME_MAP_SHIFT)+1)&(ME_MAP_SIZE-1)];
            const int br= score_map[(index+(1<<ME_MAP_SHIFT)+1)&(ME_MAP_SIZE-1)];

            for(ny= -3; ny <= 3; ny++){
                for(nx= -3; nx <= 3; nx++){
                    //FIXME this could overflow (unlikely though)
                    const int64_t t2= nx*nx*(tr + tl - 2*t) + 4*nx*(tr-tl) + 32*t;
                    const int64_t c2= nx*nx*( r +  l - 2*c) + 4*nx*( r- l) + 32*c;
                    const int64_t b2= nx*nx*(br + bl - 2*b) + 4*nx*(br-bl) + 32*b;
                    int score= (ny*ny*(b2 + t2 - 2*c2) + 4*ny*(b2 - t2) + 32*c2 + 512)>>10;
                    int i;

                    if((nx&3)==0 && (ny&3)==0) continue;

                    score += (mv_penalty[4*mx + nx - pred_x] + mv_penalty[4*my + ny - pred_y])*penalty_factor;

//                    if(nx&1) score-=1024*c->penalty_factor;
//                    if(ny&1) score-=1024*c->penalty_factor;

                    for(i=0; i<8; i++){
                        if(score < best[i]){
                            memmove(&best[i+1], &best[i], sizeof(int)*(7-i));
                            memmove(&best_pos[i+1][0], &best_pos[i][0], sizeof(int)*2*(7-i));
                            best[i]= score;
                            best_pos[i][0]= nx + 4*mx;
                            best_pos[i][1]= ny + 4*my;
                            break;
                        }
                    }
                }
            }
        }else{
            int tl;
            //FIXME this could overflow (unlikely though)
            const int cx = 4*(r - l);
            const int cx2= r + l - 2*c;
            const int cy = 4*(b - t);
            const int cy2= b + t - 2*c;
            int cxy;

            if(map[(index-(1<<ME_MAP_SHIFT)-1)&(ME_MAP_SIZE-1)] == (my<<ME_MAP_MV_BITS) + mx + map_generation && 0){ //FIXME
                tl= score_map[(index-(1<<ME_MAP_SHIFT)-1)&(ME_MAP_SIZE-1)];
            }else{
                tl= cmp(s, mx-1, my-1, 0, 0, size, h, ref_index, src_index, cmpf, chroma_cmpf, flags);//FIXME wrong if chroma me is different
            }

            cxy= 2*tl + (cx + cy)/4 - (cx2 + cy2) - 2*c;

//            av_assert2(16*cx2 + 4*cx + 32*c == 32*r);
//            av_assert2(16*cx2 - 4*cx + 32*c == 32*l);
//            av_assert2(16*cy2 + 4*cy + 32*c == 32*b);
//            av_assert2(16*cy2 - 4*cy + 32*c == 32*t);
//            av_assert2(16*cxy + 16*cy2 + 16*cx2 - 4*cy - 4*cx + 32*c == 32*tl);

            for(ny= -3; ny <= 3; ny++){
                for(nx= -3; nx <= 3; nx++){
                    //FIXME this could overflow (unlikely though)
                    int score= ny*nx*cxy + nx*nx*cx2 + ny*ny*cy2 + nx*cx + ny*cy + 32*c; //FIXME factor
                    int i;

                    if((nx&3)==0 && (ny&3)==0) continue;

                    score += 32*(mv_penalty[4*mx + nx - pred_x] + mv_penalty[4*my + ny - pred_y])*penalty_factor;
//                    if(nx&1) score-=32*c->penalty_factor;
  //                  if(ny&1) score-=32*c->penalty_factor;

                    for(i=0; i<8; i++){
                        if(score < best[i]){
                            memmove(&best[i+1], &best[i], sizeof(int)*(7-i));
                            memmove(&best_pos[i+1][0], &best_pos[i][0], sizeof(int)*2*(7-i));
                            best[i]= score;
                            best_pos[i][0]= nx + 4*mx;
                            best_pos[i][1]= ny + 4*my;
                            break;
                        }
                    }
                }
            }
        }
        for(i=0; i<subpel_quality; i++){
            nx= best_pos[i][0];
            ny= best_pos[i][1];
            CHECK_QUARTER_MV(nx&3, ny&3, nx>>2, ny>>2)
        }

//        av_assert2(bx >= xmin*4 && bx <= xmax*4 && by >= ymin*4 && by <= ymax*4);

        *mx_ptr = bx;
        *my_ptr = by;
    }else{
        *mx_ptr =4*mx;
        *my_ptr =4*my;
    }

    return dmin;
}

void ff_init_block_index(MpegEncContext *s){ //FIXME maybe rename
    const int linesize   = s->current_picture.f.linesize[0]; //not s->linesize as this would be wrong for field pics
    const int uvlinesize = s->current_picture.f.linesize[1];
    const int mb_size= 4 - s->avctx->lowres;

    s->block_index[0]= s->b8_stride*(s->mb_y*2    ) - 2 + s->mb_x*2;
    s->block_index[1]= s->b8_stride*(s->mb_y*2    ) - 1 + s->mb_x*2;
    s->block_index[2]= s->b8_stride*(s->mb_y*2 + 1) - 2 + s->mb_x*2;
    s->block_index[3]= s->b8_stride*(s->mb_y*2 + 1) - 1 + s->mb_x*2;
    s->block_index[4]= s->mb_stride*(s->mb_y + 1)                + s->b8_stride*s->mb_height*2 + s->mb_x - 1;
    s->block_index[5]= s->mb_stride*(s->mb_y + s->mb_height + 2) + s->b8_stride*s->mb_height*2 + s->mb_x - 1;
    //block_index is not used by mpeg2, so it is not affected by chroma_format

    s->dest[0] = s->current_picture.f.data[0] + ((s->mb_x - 1) <<  mb_size);
    s->dest[1] = s->current_picture.f.data[1] + ((s->mb_x - 1) << (mb_size - s->chroma_x_shift));
    s->dest[2] = s->current_picture.f.data[2] + ((s->mb_x - 1) << (mb_size - s->chroma_x_shift));

    if(!(s->pict_type==AV_PICTURE_TYPE_B && s->avctx->draw_horiz_band && s->picture_structure==PICT_FRAME))
    {
        if(s->picture_structure==PICT_FRAME){
        s->dest[0] += s->mb_y *   linesize << mb_size;
        s->dest[1] += s->mb_y * uvlinesize << (mb_size - s->chroma_y_shift);
        s->dest[2] += s->mb_y * uvlinesize << (mb_size - s->chroma_y_shift);
        }else{
            s->dest[0] += (s->mb_y>>1) *   linesize << mb_size;
            s->dest[1] += (s->mb_y>>1) * uvlinesize << (mb_size - s->chroma_y_shift);
            s->dest[2] += (s->mb_y>>1) * uvlinesize << (mb_size - s->chroma_y_shift);
//            av_assert1((s->mb_y&1) == (s->picture_structure == PICT_BOTTOM_FIELD));
        }
    }
}



static inline int get_mb_score(MpegEncContext *s, int mx, int my,
                               int src_index, int ref_index, int size,
                               int h, int add_rate)
{
//    const int check_luma= s->dsp.me_sub_cmp != s->dsp.mb_cmp;
    MotionEstContext * const c= &s->me;
    const int penalty_factor= c->mb_penalty_factor;
    const int flags= c->mb_flags;
    const int qpel= flags & FLAG_QPEL;
    const int mask= 1+2*qpel;
    me_cmp_func cmp_sub, chroma_cmp_sub;
    int d;

    LOAD_COMMON

 //FIXME factorize

    cmp_sub= s->dsp.mb_cmp[size];
    chroma_cmp_sub= s->dsp.mb_cmp[size+1];

    d= cmp(s, mx>>(qpel+1), my>>(qpel+1), mx&mask, my&mask, size, h, ref_index, src_index, cmp_sub, chroma_cmp_sub, flags);
    //FIXME check cbp before adding penalty for (0,0) vector
    if(add_rate && (mx || my || size>0))
        d += (mv_penalty[mx - pred_x] + mv_penalty[my - pred_y])*penalty_factor;

    return d;
}

static inline int direct_search(MpegEncContext * s, int mb_x, int mb_y)
{
    MotionEstContext * const c= &s->me;
    int P[10][2];
    const int mot_stride = s->mb_stride;
    const int mot_xy = mb_y*mot_stride + mb_x;
    const int shift= 1+s->quarter_sample;
    int dmin, i;
    const int time_pp= s->pp_time;
    const int time_pb= s->pb_time;
    int mx, my, xmin, xmax, ymin, ymax;
    int16_t (*mv_table)[2]= s->b_direct_mv_table;

    c->current_mv_penalty= c->mv_penalty[1] + MAX_MV;
    ymin= xmin=(-32)>>shift;
    ymax= xmax=   31>>shift;

    if (IS_8X8(s->next_picture.f.mb_type[mot_xy])) {
        s->mv_type= MV_TYPE_8X8;
    }else{
        s->mv_type= MV_TYPE_16X16;
    }

    for(i=0; i<4; i++){
        int index= s->block_index[i];
        int min, max;

        c->co_located_mv[i][0] = s->next_picture.f.motion_val[0][index][0];
        c->co_located_mv[i][1] = s->next_picture.f.motion_val[0][index][1];
        c->direct_basis_mv[i][0]= c->co_located_mv[i][0]*time_pb/time_pp + ((i& 1)<<(shift+3));
        c->direct_basis_mv[i][1]= c->co_located_mv[i][1]*time_pb/time_pp + ((i>>1)<<(shift+3));
//        c->direct_basis_mv[1][i][0]= c->co_located_mv[i][0]*(time_pb - time_pp)/time_pp + ((i &1)<<(shift+3);
//        c->direct_basis_mv[1][i][1]= c->co_located_mv[i][1]*(time_pb - time_pp)/time_pp + ((i>>1)<<(shift+3);

        max= FFMAX(c->direct_basis_mv[i][0], c->direct_basis_mv[i][0] - c->co_located_mv[i][0])>>shift;
        min= FFMIN(c->direct_basis_mv[i][0], c->direct_basis_mv[i][0] - c->co_located_mv[i][0])>>shift;
        max+= 16*mb_x + 1; // +-1 is for the simpler rounding
        min+= 16*mb_x - 1;
        xmax= FFMIN(xmax, s->width - max);
        xmin= FFMAX(xmin, - 16     - min);

        max= FFMAX(c->direct_basis_mv[i][1], c->direct_basis_mv[i][1] - c->co_located_mv[i][1])>>shift;
        min= FFMIN(c->direct_basis_mv[i][1], c->direct_basis_mv[i][1] - c->co_located_mv[i][1])>>shift;
        max+= 16*mb_y + 1; // +-1 is for the simpler rounding
        min+= 16*mb_y - 1;
        ymax= FFMIN(ymax, s->height - max);
        ymin= FFMAX(ymin, - 16      - min);

        if(s->mv_type == MV_TYPE_16X16) break;
    }

//    av_assert2(xmax <= 15 && ymax <= 15 && xmin >= -16 && ymin >= -16);

    if(xmax < 0 || xmin >0 || ymax < 0 || ymin > 0){
        s->b_direct_mv_table[mot_xy][0]= 0;
        s->b_direct_mv_table[mot_xy][1]= 0;

        return 256*256*256*64;
    }

    c->xmin= xmin;
    c->ymin= ymin;
    c->xmax= xmax;
    c->ymax= ymax;
    c->flags     |= FLAG_DIRECT;
    c->sub_flags |= FLAG_DIRECT;
    c->pred_x=0;
    c->pred_y=0;

    P_LEFT[0]        = av_clip_c(mv_table[mot_xy - 1][0], xmin<<shift, xmax<<shift);
    P_LEFT[1]        = av_clip_c(mv_table[mot_xy - 1][1], ymin<<shift, ymax<<shift);

    /* special case for first line */
    if (!s->first_slice_line) { //FIXME maybe allow this over thread boundary as it is clipped
        P_TOP[0]      = av_clip_c(mv_table[mot_xy - mot_stride             ][0], xmin<<shift, xmax<<shift);
        P_TOP[1]      = av_clip_c(mv_table[mot_xy - mot_stride             ][1], ymin<<shift, ymax<<shift);
        P_TOPRIGHT[0] = av_clip_c(mv_table[mot_xy - mot_stride + 1         ][0], xmin<<shift, xmax<<shift);
        P_TOPRIGHT[1] = av_clip_c(mv_table[mot_xy - mot_stride + 1         ][1], ymin<<shift, ymax<<shift);

        P_MEDIAN[0]= mid_pred(P_LEFT[0], P_TOP[0], P_TOPRIGHT[0]);
        P_MEDIAN[1]= mid_pred(P_LEFT[1], P_TOP[1], P_TOPRIGHT[1]);
    }

    dmin = ff_epzs_motion_search(s, &mx, &my, P, 0, 0, mv_table, 1<<(16-shift), 0, 16);
    if(c->sub_flags&FLAG_QPEL)
        dmin = qpel_motion_search(s, &mx, &my, dmin, 0, 0, 0, 16);
    else
        dmin = hpel_motion_search(s, &mx, &my, dmin, 0, 0, 0, 16);

    if(c->avctx->me_sub_cmp != c->avctx->mb_cmp && !c->skip)
        dmin= get_mb_score(s, mx, my, 0, 0, 0, 16, 1);

    get_limits(s, 16*mb_x, 16*mb_y); //restore c->?min/max, maybe not needed

    mv_table[mot_xy][0]= mx;
    mv_table[mot_xy][1]= my;
    c->flags     &= ~FLAG_DIRECT;
    c->sub_flags &= ~FLAG_DIRECT;

    return dmin;
}

static int ff_estimate_motion_b(MpegEncContext * s,
                       int mb_x, int mb_y, int16_t (*mv_table)[2], int ref_index, int f_code)
{
	LOGD("ff_estimate_motion_b Called\n");
    MotionEstContext * const c= &s->me;
    int mx, my, dmin;
    int P[10][2];
    const int shift= 1+s->quarter_sample;
    const int mot_stride = s->mb_stride;
    const int mot_xy = mb_y*mot_stride + mb_x;
    uint8_t * const mv_penalty= c->mv_penalty[f_code] + MAX_MV;
    int mv_scale;

    c->penalty_factor    = get_penalty_factor(s->lambda, s->lambda2, c->avctx->me_cmp);
    c->sub_penalty_factor= get_penalty_factor(s->lambda, s->lambda2, c->avctx->me_sub_cmp);
    c->mb_penalty_factor = get_penalty_factor(s->lambda, s->lambda2, c->avctx->mb_cmp);
    c->current_mv_penalty= mv_penalty;

    get_limits(s, 16*mb_x, 16*mb_y);

    switch(s->me_method) {
    case ME_ZERO:
    default:
        mx   = 0;
        my   = 0;
        dmin = 0;
        break;
    case ME_X1:
    case ME_EPZS:
        P_LEFT[0] = mv_table[mot_xy - 1][0];
        P_LEFT[1] = mv_table[mot_xy - 1][1];

        if (P_LEFT[0] > (c->xmax << shift)) P_LEFT[0] = (c->xmax << shift);

        /* special case for first line */
        if (!s->first_slice_line) {
            P_TOP[0]      = mv_table[mot_xy - mot_stride    ][0];
            P_TOP[1]      = mv_table[mot_xy - mot_stride    ][1];
            P_TOPRIGHT[0] = mv_table[mot_xy - mot_stride + 1][0];
            P_TOPRIGHT[1] = mv_table[mot_xy - mot_stride + 1][1];
            if (P_TOP[1] > (c->ymax << shift)) P_TOP[1] = (c->ymax << shift);
            if (P_TOPRIGHT[0] < (c->xmin << shift)) P_TOPRIGHT[0] = (c->xmin << shift);
            if (P_TOPRIGHT[1] > (c->ymax << shift)) P_TOPRIGHT[1] = (c->ymax << shift);

            P_MEDIAN[0] = mid_pred(P_LEFT[0], P_TOP[0], P_TOPRIGHT[0]);
            P_MEDIAN[1] = mid_pred(P_LEFT[1], P_TOP[1], P_TOPRIGHT[1]);
        }
        c->pred_x = P_LEFT[0];
        c->pred_y = P_LEFT[1];

        if(mv_table == s->b_forw_mv_table){
            mv_scale= (s->pb_time<<16) / (s->pp_time<<shift);
        }else{
            mv_scale= ((s->pb_time - s->pp_time)<<16) / (s->pp_time<<shift);
        }

        dmin = ff_epzs_motion_search(s, &mx, &my, P, 0, ref_index, s->p_mv_table, mv_scale, 0, 16);

        break;
    }

    dmin= c->sub_motion_search(s, &mx, &my, dmin, 0, ref_index, 0, 16);

    if(c->avctx->me_sub_cmp != c->avctx->mb_cmp && !c->skip)
        dmin= get_mb_score(s, mx, my, 0, ref_index, 0, 16, 1);

//    s->mb_type[mb_y*s->mb_width + mb_x]= mb_type;
    mv_table[mot_xy][0]= mx;
    mv_table[mot_xy][1]= my;

    return dmin;
}


//try to merge with above FIXME (needs PSNR test)
static int epzs_motion_search2(MpegEncContext * s,
                             int *mx_ptr, int *my_ptr, int P[10][2],
                             int src_index, int ref_index, int16_t (*last_mv)[2],
                             int ref_mv_scale)
{
    MotionEstContext * const c= &s->me;
    int best[2]={0, 0};
    int d, dmin;
    unsigned map_generation;
    const int penalty_factor= c->penalty_factor;
    const int size=0; //FIXME pass as arg
    const int h=8;
    const int ref_mv_stride= s->mb_stride;
    const int ref_mv_xy= s->mb_x + s->mb_y *ref_mv_stride;
    me_cmp_func cmpf, chroma_cmpf;
    LOAD_COMMON
    int flags= c->flags;
    LOAD_COMMON2

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    map_generation= update_map_generation(c);

    dmin = 1000000;

    /* first line */
    if (s->first_slice_line) {
        CHECK_MV(P_LEFT[0]>>shift, P_LEFT[1]>>shift)
        CHECK_CLIPPED_MV((last_mv[ref_mv_xy][0]*ref_mv_scale + (1<<15))>>16,
                        (last_mv[ref_mv_xy][1]*ref_mv_scale + (1<<15))>>16)
        CHECK_MV(P_MV1[0]>>shift, P_MV1[1]>>shift)
    }else{
        CHECK_MV(P_MV1[0]>>shift, P_MV1[1]>>shift)
        //FIXME try some early stop
        CHECK_MV(P_MEDIAN[0]>>shift, P_MEDIAN[1]>>shift)
        CHECK_MV(P_LEFT[0]>>shift, P_LEFT[1]>>shift)
        CHECK_MV(P_TOP[0]>>shift, P_TOP[1]>>shift)
        CHECK_MV(P_TOPRIGHT[0]>>shift, P_TOPRIGHT[1]>>shift)
        CHECK_CLIPPED_MV((last_mv[ref_mv_xy][0]*ref_mv_scale + (1<<15))>>16,
                        (last_mv[ref_mv_xy][1]*ref_mv_scale + (1<<15))>>16)
    }
    if(dmin>64*4){
        CHECK_CLIPPED_MV((last_mv[ref_mv_xy+1][0]*ref_mv_scale + (1<<15))>>16,
                        (last_mv[ref_mv_xy+1][1]*ref_mv_scale + (1<<15))>>16)
        if(s->mb_y+1<s->end_mb_y)  //FIXME replace at least with last_slice_line
            CHECK_CLIPPED_MV((last_mv[ref_mv_xy+ref_mv_stride][0]*ref_mv_scale + (1<<15))>>16,
                            (last_mv[ref_mv_xy+ref_mv_stride][1]*ref_mv_scale + (1<<15))>>16)
    }

    dmin= diamond_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);

    *mx_ptr= best[0];
    *my_ptr= best[1];

    return dmin;
}


static inline void init_interlaced_ref(MpegEncContext *s, int ref_index){
    MotionEstContext * const c= &s->me;

    c->ref[1+ref_index][0] = c->ref[0+ref_index][0] + s->linesize;
    c->src[1][0] = c->src[0][0] + s->linesize;
    if(c->flags & FLAG_CHROMA){
        c->ref[1+ref_index][1] = c->ref[0+ref_index][1] + s->uvlinesize;
        c->ref[1+ref_index][2] = c->ref[0+ref_index][2] + s->uvlinesize;
        c->src[1][1] = c->src[0][1] + s->uvlinesize;
        c->src[1][2] = c->src[0][2] + s->uvlinesize;
    }
}

static int interlaced_search(MpegEncContext *s, int ref_index,
                             int16_t (*mv_tables[2][2])[2], uint8_t *field_select_tables[2], int mx, int my, int user_field_select)
{
    MotionEstContext * const c= &s->me;
    const int size=0;
    const int h=8;
    int block;
    int P[10][2];
    uint8_t * const mv_penalty= c->current_mv_penalty;
    int same=1;
    const int stride= 2*s->linesize;
    int dmin_sum= 0;
    const int mot_stride= s->mb_stride;
    const int xy= s->mb_x + s->mb_y*mot_stride;

    c->ymin>>=1;
    c->ymax>>=1;
    c->stride<<=1;
    c->uvstride<<=1;
    init_interlaced_ref(s, ref_index);

    for(block=0; block<2; block++){
        int field_select;
        int best_dmin= INT_MAX;
        int best_field= -1;

        for(field_select=0; field_select<2; field_select++){
            int dmin, mx_i, my_i;
            int16_t (*mv_table)[2]= mv_tables[block][field_select];

            if(user_field_select){
//                av_assert1(field_select==0 || field_select==1);
//                av_assert1(field_select_tables[block][xy]==0 || field_select_tables[block][xy]==1);
                if(field_select_tables[block][xy] != field_select)
                    continue;
            }

            P_LEFT[0] = mv_table[xy - 1][0];
            P_LEFT[1] = mv_table[xy - 1][1];
            if(P_LEFT[0]       > (c->xmax<<1)) P_LEFT[0]       = (c->xmax<<1);

            c->pred_x= P_LEFT[0];
            c->pred_y= P_LEFT[1];

            if(!s->first_slice_line){
                P_TOP[0]      = mv_table[xy - mot_stride][0];
                P_TOP[1]      = mv_table[xy - mot_stride][1];
                P_TOPRIGHT[0] = mv_table[xy - mot_stride + 1][0];
                P_TOPRIGHT[1] = mv_table[xy - mot_stride + 1][1];
                if(P_TOP[1]      > (c->ymax<<1)) P_TOP[1]     = (c->ymax<<1);
                if(P_TOPRIGHT[0] < (c->xmin<<1)) P_TOPRIGHT[0]= (c->xmin<<1);
                if(P_TOPRIGHT[0] > (c->xmax<<1)) P_TOPRIGHT[0]= (c->xmax<<1);
                if(P_TOPRIGHT[1] > (c->ymax<<1)) P_TOPRIGHT[1]= (c->ymax<<1);

                P_MEDIAN[0]= mid_pred(P_LEFT[0], P_TOP[0], P_TOPRIGHT[0]);
                P_MEDIAN[1]= mid_pred(P_LEFT[1], P_TOP[1], P_TOPRIGHT[1]);
            }
            P_MV1[0]= mx; //FIXME not correct if block != field_select
            P_MV1[1]= my / 2;

            dmin = epzs_motion_search2(s, &mx_i, &my_i, P, block, field_select+ref_index, mv_table, (1<<16)>>1);

            dmin= c->sub_motion_search(s, &mx_i, &my_i, dmin, block, field_select+ref_index, size, h);

            mv_table[xy][0]= mx_i;
            mv_table[xy][1]= my_i;

            if(s->dsp.me_sub_cmp[0] != s->dsp.mb_cmp[0]){
                int dxy;

                //FIXME chroma ME
                uint8_t *ref= c->ref[field_select+ref_index][0] + (mx_i>>1) + (my_i>>1)*stride;
                dxy = ((my_i & 1) << 1) | (mx_i & 1);

                if(s->no_rounding){
                    s->dsp.put_no_rnd_pixels_tab[size][dxy](c->scratchpad, ref    , stride, h);
                }else{
                    s->dsp.put_pixels_tab       [size][dxy](c->scratchpad, ref    , stride, h);
                }
                dmin= s->dsp.mb_cmp[size](s, c->src[block][0], c->scratchpad, stride, h);
                dmin+= (mv_penalty[mx_i-c->pred_x] + mv_penalty[my_i-c->pred_y] + 1)*c->mb_penalty_factor;
            }else
                dmin+= c->mb_penalty_factor; //field_select bits

            dmin += field_select != block; //slightly prefer same field

            if(dmin < best_dmin){
                best_dmin= dmin;
                best_field= field_select;
            }
        }
        {
            int16_t (*mv_table)[2]= mv_tables[block][best_field];

            if(mv_table[xy][0] != mx) same=0; //FIXME check if these checks work and are any good at all
            if(mv_table[xy][1]&1) same=0;
            if(mv_table[xy][1]*2 != my) same=0;
            if(best_field != block) same=0;
        }

        field_select_tables[block][xy]= best_field;
        dmin_sum += best_dmin;
    }

    c->ymin<<=1;
    c->ymax<<=1;
    c->stride>>=1;
    c->uvstride>>=1;

    if(same)
        return INT_MAX;

    switch(c->avctx->mb_cmp&0xFF){
    /*case FF_CMP_SSE:
        return dmin_sum+ 32*s->qscale*s->qscale;*/
    case FF_CMP_RD:
        return dmin_sum;
    default:
        return dmin_sum+ 11*c->mb_penalty_factor;
    }
}

static inline int check_bidir_mv(MpegEncContext * s,
                   int motion_fx, int motion_fy,
                   int motion_bx, int motion_by,
                   int pred_fx, int pred_fy,
                   int pred_bx, int pred_by,
                   int size, int h)
{
    //FIXME optimize?
    //FIXME better f_code prediction (max mv & distance)
    //FIXME pointers
    MotionEstContext * const c= &s->me;
    uint8_t * const mv_penalty_f= c->mv_penalty[s->f_code] + MAX_MV; // f_code of the prev frame
    uint8_t * const mv_penalty_b= c->mv_penalty[s->b_code] + MAX_MV; // f_code of the prev frame
    int stride= c->stride;
    uint8_t *dest_y = c->scratchpad;
    uint8_t *ptr;
    int dxy;
    int src_x, src_y;
    int fbmin;
    uint8_t **src_data= c->src[0];
    uint8_t **ref_data= c->ref[0];
    uint8_t **ref2_data= c->ref[2];

    if(s->quarter_sample){
        dxy = ((motion_fy & 3) << 2) | (motion_fx & 3);
        src_x = motion_fx >> 2;
        src_y = motion_fy >> 2;

        ptr = ref_data[0] + (src_y * stride) + src_x;
        s->dsp.put_qpel_pixels_tab[0][dxy](dest_y    , ptr    , stride);

        dxy = ((motion_by & 3) << 2) | (motion_bx & 3);
        src_x = motion_bx >> 2;
        src_y = motion_by >> 2;

        ptr = ref2_data[0] + (src_y * stride) + src_x;
        s->dsp.avg_qpel_pixels_tab[size][dxy](dest_y    , ptr    , stride);
    }else{
        dxy = ((motion_fy & 1) << 1) | (motion_fx & 1);
        src_x = motion_fx >> 1;
        src_y = motion_fy >> 1;

        ptr = ref_data[0] + (src_y * stride) + src_x;
        s->dsp.put_pixels_tab[size][dxy](dest_y    , ptr    , stride, h);

        dxy = ((motion_by & 1) << 1) | (motion_bx & 1);
        src_x = motion_bx >> 1;
        src_y = motion_by >> 1;

        ptr = ref2_data[0] + (src_y * stride) + src_x;
        s->dsp.avg_pixels_tab[size][dxy](dest_y    , ptr    , stride, h);
    }

    fbmin = (mv_penalty_f[motion_fx-pred_fx] + mv_penalty_f[motion_fy-pred_fy])*c->mb_penalty_factor
           +(mv_penalty_b[motion_bx-pred_bx] + mv_penalty_b[motion_by-pred_by])*c->mb_penalty_factor
           + s->dsp.mb_cmp[size](s, src_data[0], dest_y, stride, h); //FIXME new_pic

    if(c->avctx->mb_cmp&FF_CMP_CHROMA){
    }
    //FIXME CHROMA !!!

    return fbmin;
}

/* refine the bidir vectors in hq mode and return the score in both lq & hq mode*/
static inline int bidir_refine(MpegEncContext * s, int mb_x, int mb_y)
{
    MotionEstContext * const c= &s->me;
    const int mot_stride = s->mb_stride;
    const int xy = mb_y *mot_stride + mb_x;
    int fbmin;
    int pred_fx= s->b_bidir_forw_mv_table[xy-1][0];
    int pred_fy= s->b_bidir_forw_mv_table[xy-1][1];
    int pred_bx= s->b_bidir_back_mv_table[xy-1][0];
    int pred_by= s->b_bidir_back_mv_table[xy-1][1];
    int motion_fx= s->b_bidir_forw_mv_table[xy][0]= s->b_forw_mv_table[xy][0];
    int motion_fy= s->b_bidir_forw_mv_table[xy][1]= s->b_forw_mv_table[xy][1];
    int motion_bx= s->b_bidir_back_mv_table[xy][0]= s->b_back_mv_table[xy][0];
    int motion_by= s->b_bidir_back_mv_table[xy][1]= s->b_back_mv_table[xy][1];
    const int flags= c->sub_flags;
    const int qpel= flags&FLAG_QPEL;
    const int shift= 1+qpel;
    const int xmin= c->xmin<<shift;
    const int ymin= c->ymin<<shift;
    const int xmax= c->xmax<<shift;
    const int ymax= c->ymax<<shift;
#define HASH(fx,fy,bx,by) ((fx)+17*(fy)+63*(bx)+117*(by))
#define HASH8(fx,fy,bx,by) ((uint8_t)HASH(fx,fy,bx,by))
    int hashidx= HASH(motion_fx,motion_fy, motion_bx, motion_by);
    uint8_t map[256] = { 0 };

    map[hashidx&255] = 1;

    fbmin= check_bidir_mv(s, motion_fx, motion_fy,
                          motion_bx, motion_by,
                          pred_fx, pred_fy,
                          pred_bx, pred_by,
                          0, 16);

    if(s->avctx->bidir_refine){
        int end;
        static const uint8_t limittab[5]={0,8,32,64,80};
        const int limit= limittab[s->avctx->bidir_refine];
        static const int8_t vect[][4]={
{ 0, 0, 0, 1}, { 0, 0, 0,-1}, { 0, 0, 1, 0}, { 0, 0,-1, 0}, { 0, 1, 0, 0}, { 0,-1, 0, 0}, { 1, 0, 0, 0}, {-1, 0, 0, 0},

{ 0, 0, 1, 1}, { 0, 0,-1,-1}, { 0, 1, 1, 0}, { 0,-1,-1, 0}, { 1, 1, 0, 0}, {-1,-1, 0, 0}, { 1, 0, 0, 1}, {-1, 0, 0,-1},
{ 0, 1, 0, 1}, { 0,-1, 0,-1}, { 1, 0, 1, 0}, {-1, 0,-1, 0},
{ 0, 0,-1, 1}, { 0, 0, 1,-1}, { 0,-1, 1, 0}, { 0, 1,-1, 0}, {-1, 1, 0, 0}, { 1,-1, 0, 0}, { 1, 0, 0,-1}, {-1, 0, 0, 1},
{ 0,-1, 0, 1}, { 0, 1, 0,-1}, {-1, 0, 1, 0}, { 1, 0,-1, 0},

{ 0, 1, 1, 1}, { 0,-1,-1,-1}, { 1, 1, 1, 0}, {-1,-1,-1, 0}, { 1, 1, 0, 1}, {-1,-1, 0,-1}, { 1, 0, 1, 1}, {-1, 0,-1,-1},
{ 0,-1, 1, 1}, { 0, 1,-1,-1}, {-1, 1, 1, 0}, { 1,-1,-1, 0}, { 1, 1, 0,-1}, {-1,-1, 0, 1}, { 1, 0,-1, 1}, {-1, 0, 1,-1},
{ 0, 1,-1, 1}, { 0,-1, 1,-1}, { 1,-1, 1, 0}, {-1, 1,-1, 0}, {-1, 1, 0, 1}, { 1,-1, 0,-1}, { 1, 0, 1,-1}, {-1, 0,-1, 1},
{ 0, 1, 1,-1}, { 0,-1,-1, 1}, { 1, 1,-1, 0}, {-1,-1, 1, 0}, { 1,-1, 0, 1}, {-1, 1, 0,-1}, {-1, 0, 1, 1}, { 1, 0,-1,-1},

{ 1, 1, 1, 1}, {-1,-1,-1,-1},
{ 1, 1, 1,-1}, {-1,-1,-1, 1}, { 1, 1,-1, 1}, {-1,-1, 1,-1}, { 1,-1, 1, 1}, {-1, 1,-1,-1}, {-1, 1, 1, 1}, { 1,-1,-1,-1},
{ 1, 1,-1,-1}, {-1,-1, 1, 1}, { 1,-1,-1, 1}, {-1, 1, 1,-1}, { 1,-1, 1,-1}, {-1, 1,-1, 1},
        };
        static const uint8_t hash[]={
HASH8( 0, 0, 0, 1), HASH8( 0, 0, 0,-1), HASH8( 0, 0, 1, 0), HASH8( 0, 0,-1, 0), HASH8( 0, 1, 0, 0), HASH8( 0,-1, 0, 0), HASH8( 1, 0, 0, 0), HASH8(-1, 0, 0, 0),

HASH8( 0, 0, 1, 1), HASH8( 0, 0,-1,-1), HASH8( 0, 1, 1, 0), HASH8( 0,-1,-1, 0), HASH8( 1, 1, 0, 0), HASH8(-1,-1, 0, 0), HASH8( 1, 0, 0, 1), HASH8(-1, 0, 0,-1),
HASH8( 0, 1, 0, 1), HASH8( 0,-1, 0,-1), HASH8( 1, 0, 1, 0), HASH8(-1, 0,-1, 0),
HASH8( 0, 0,-1, 1), HASH8( 0, 0, 1,-1), HASH8( 0,-1, 1, 0), HASH8( 0, 1,-1, 0), HASH8(-1, 1, 0, 0), HASH8( 1,-1, 0, 0), HASH8( 1, 0, 0,-1), HASH8(-1, 0, 0, 1),
HASH8( 0,-1, 0, 1), HASH8( 0, 1, 0,-1), HASH8(-1, 0, 1, 0), HASH8( 1, 0,-1, 0),

HASH8( 0, 1, 1, 1), HASH8( 0,-1,-1,-1), HASH8( 1, 1, 1, 0), HASH8(-1,-1,-1, 0), HASH8( 1, 1, 0, 1), HASH8(-1,-1, 0,-1), HASH8( 1, 0, 1, 1), HASH8(-1, 0,-1,-1),
HASH8( 0,-1, 1, 1), HASH8( 0, 1,-1,-1), HASH8(-1, 1, 1, 0), HASH8( 1,-1,-1, 0), HASH8( 1, 1, 0,-1), HASH8(-1,-1, 0, 1), HASH8( 1, 0,-1, 1), HASH8(-1, 0, 1,-1),
HASH8( 0, 1,-1, 1), HASH8( 0,-1, 1,-1), HASH8( 1,-1, 1, 0), HASH8(-1, 1,-1, 0), HASH8(-1, 1, 0, 1), HASH8( 1,-1, 0,-1), HASH8( 1, 0, 1,-1), HASH8(-1, 0,-1, 1),
HASH8( 0, 1, 1,-1), HASH8( 0,-1,-1, 1), HASH8( 1, 1,-1, 0), HASH8(-1,-1, 1, 0), HASH8( 1,-1, 0, 1), HASH8(-1, 1, 0,-1), HASH8(-1, 0, 1, 1), HASH8( 1, 0,-1,-1),

HASH8( 1, 1, 1, 1), HASH8(-1,-1,-1,-1),
HASH8( 1, 1, 1,-1), HASH8(-1,-1,-1, 1), HASH8( 1, 1,-1, 1), HASH8(-1,-1, 1,-1), HASH8( 1,-1, 1, 1), HASH8(-1, 1,-1,-1), HASH8(-1, 1, 1, 1), HASH8( 1,-1,-1,-1),
HASH8( 1, 1,-1,-1), HASH8(-1,-1, 1, 1), HASH8( 1,-1,-1, 1), HASH8(-1, 1, 1,-1), HASH8( 1,-1, 1,-1), HASH8(-1, 1,-1, 1),
};

#define CHECK_BIDIR(fx,fy,bx,by)\
    if( !map[(hashidx+HASH(fx,fy,bx,by))&255]\
       &&(fx<=0 || motion_fx+fx<=xmax) && (fy<=0 || motion_fy+fy<=ymax) && (bx<=0 || motion_bx+bx<=xmax) && (by<=0 || motion_by+by<=ymax)\
       &&(fx>=0 || motion_fx+fx>=xmin) && (fy>=0 || motion_fy+fy>=ymin) && (bx>=0 || motion_bx+bx>=xmin) && (by>=0 || motion_by+by>=ymin)){\
        int score;\
        map[(hashidx+HASH(fx,fy,bx,by))&255] = 1;\
        score= check_bidir_mv(s, motion_fx+fx, motion_fy+fy, motion_bx+bx, motion_by+by, pred_fx, pred_fy, pred_bx, pred_by, 0, 16);\
        if(score < fbmin){\
            hashidx += HASH(fx,fy,bx,by);\
            fbmin= score;\
            motion_fx+=fx;\
            motion_fy+=fy;\
            motion_bx+=bx;\
            motion_by+=by;\
            end=0;\
        }\
    }
#define CHECK_BIDIR2(a,b,c,d)\
CHECK_BIDIR(a,b,c,d)\
CHECK_BIDIR(-(a),-(b),-(c),-(d))

        do{
            int i;
            int borderdist=0;
            end=1;

            CHECK_BIDIR2(0,0,0,1)
            CHECK_BIDIR2(0,0,1,0)
            CHECK_BIDIR2(0,1,0,0)
            CHECK_BIDIR2(1,0,0,0)

            for(i=8; i<limit; i++){
                int fx= motion_fx+vect[i][0];
                int fy= motion_fy+vect[i][1];
                int bx= motion_bx+vect[i][2];
                int by= motion_by+vect[i][3];
                if(borderdist<=0){
                    int a= (xmax - FFMAX(fx,bx))|(FFMIN(fx,bx) - xmin);
                    int b= (ymax - FFMAX(fy,by))|(FFMIN(fy,by) - ymin);
                    if((a|b) < 0)
                        map[(hashidx+hash[i])&255] = 1;
                }
                if(!map[(hashidx+hash[i])&255]){
                    int score;
                    map[(hashidx+hash[i])&255] = 1;
                    score= check_bidir_mv(s, fx, fy, bx, by, pred_fx, pred_fy, pred_bx, pred_by, 0, 16);
                    if(score < fbmin){
                        hashidx += hash[i];
                        fbmin= score;
                        motion_fx=fx;
                        motion_fy=fy;
                        motion_bx=bx;
                        motion_by=by;
                        end=0;
                        borderdist--;
                        if(borderdist<=0){
                            int a= FFMIN(xmax - FFMAX(fx,bx), FFMIN(fx,bx) - xmin);
                            int b= FFMIN(ymax - FFMAX(fy,by), FFMIN(fy,by) - ymin);
                            borderdist= FFMIN(a,b);
                        }
                    }
                }
            }
        }while(!end);
    }

    s->b_bidir_forw_mv_table[xy][0]= motion_fx;
    s->b_bidir_forw_mv_table[xy][1]= motion_fy;
    s->b_bidir_back_mv_table[xy][0]= motion_bx;
    s->b_bidir_back_mv_table[xy][1]= motion_by;

    return fbmin;
}

void ff_estimate_b_frame_motion(MpegEncContext * s,
                             int mb_x, int mb_y)
{
	LOGD("ff_estimate_b_frame_motion Called\n");
    MotionEstContext * const c= &s->me;
    const int penalty_factor= c->mb_penalty_factor;
    int fmin, bmin, dmin, fbmin, bimin, fimin;
    int type=0;
    const int xy = mb_y*s->mb_stride + mb_x;
    init_ref(c, s->new_picture.f.data, s->last_picture.f.data,
             s->next_picture.f.data, 16 * mb_x, 16 * mb_y, 2);

    get_limits(s, 16*mb_x, 16*mb_y);

    c->skip=0;

    if (s->codec_id == AV_CODEC_ID_MPEG4 && s->next_picture.f.mbskip_table[xy]) {
        int score= direct_search(s, mb_x, mb_y); //FIXME just check 0,0

        score= ((unsigned)(score*score + 128*256))>>16;
        c->mc_mb_var_sum_temp += score;
        s->current_picture.mc_mb_var[mb_y*s->mb_stride + mb_x] = score; //FIXME use SSE
        s->mb_type[mb_y*s->mb_stride + mb_x]= CANDIDATE_MB_TYPE_DIRECT0;

        return;
    }

    if(c->avctx->me_threshold){
        int vard= check_input_motion(s, mb_x, mb_y, 0);

        if((vard+128)>>8 < c->avctx->me_threshold){
//            pix = c->src[0][0];
//            sum = s->dsp.pix_sum(pix, s->linesize);
//            varc = s->dsp.pix_norm1(pix, s->linesize) - (((unsigned)(sum*sum))>>8) + 500;

//            pic->mb_var   [s->mb_stride * mb_y + mb_x] = (varc+128)>>8;
             s->current_picture.mc_mb_var[s->mb_stride * mb_y + mb_x] = (vard+128)>>8;
/*            pic->mb_mean  [s->mb_stride * mb_y + mb_x] = (sum+128)>>8;
            c->mb_var_sum_temp    += (varc+128)>>8;*/
            c->mc_mb_var_sum_temp += (vard+128)>>8;
/*            if (vard <= 64<<8 || vard < varc) {
                c->scene_change_score+= ff_sqrt(vard) - ff_sqrt(varc);
            }else{
                c->scene_change_score+= s->qscale * s->avctx->scenechange_factor;
            }*/
            return;
        }


        if((vard+128)>>8 < c->avctx->mb_threshold){
            type= s->mb_type[mb_y*s->mb_stride + mb_x];
            if(type == CANDIDATE_MB_TYPE_DIRECT){
                direct_search(s, mb_x, mb_y);
            }
            if(type == CANDIDATE_MB_TYPE_FORWARD || type == CANDIDATE_MB_TYPE_BIDIR){
                c->skip=0;
                ff_estimate_motion_b(s, mb_x, mb_y, s->b_forw_mv_table, 0, s->f_code);
            }
            if(type == CANDIDATE_MB_TYPE_BACKWARD || type == CANDIDATE_MB_TYPE_BIDIR){
                c->skip=0;
                ff_estimate_motion_b(s, mb_x, mb_y, s->b_back_mv_table, 2, s->b_code);
            }
            if(type == CANDIDATE_MB_TYPE_FORWARD_I || type == CANDIDATE_MB_TYPE_BIDIR_I){
                c->skip=0;
                c->current_mv_penalty= c->mv_penalty[s->f_code] + MAX_MV;
                interlaced_search(s, 0,
                                        s->b_field_mv_table[0], s->b_field_select_table[0],
                                        s->b_forw_mv_table[xy][0], s->b_forw_mv_table[xy][1], 1);
            }
            if(type == CANDIDATE_MB_TYPE_BACKWARD_I || type == CANDIDATE_MB_TYPE_BIDIR_I){
                c->skip=0;
                c->current_mv_penalty= c->mv_penalty[s->b_code] + MAX_MV;
                interlaced_search(s, 2,
                                        s->b_field_mv_table[1], s->b_field_select_table[1],
                                        s->b_back_mv_table[xy][0], s->b_back_mv_table[xy][1], 1);
            }
            return;
        }
    }

    if (s->codec_id == AV_CODEC_ID_MPEG4)
        dmin= direct_search(s, mb_x, mb_y);
    else
        dmin= INT_MAX;
//FIXME penalty stuff for non mpeg4
    c->skip=0;
    fmin= ff_estimate_motion_b(s, mb_x, mb_y, s->b_forw_mv_table, 0, s->f_code) + 3*penalty_factor;

    c->skip=0;
    bmin= ff_estimate_motion_b(s, mb_x, mb_y, s->b_back_mv_table, 2, s->b_code) + 2*penalty_factor;
//    av_dlog(s, " %d %d ", s->b_forw_mv_table[xy][0], s->b_forw_mv_table[xy][1]);

    c->skip=0;
    fbmin= bidir_refine(s, mb_x, mb_y) + penalty_factor;
//    av_dlog(s, "%d %d %d %d\n", dmin, fmin, bmin, fbmin);

    if(s->flags & CODEC_FLAG_INTERLACED_ME){
//FIXME mb type penalty
        c->skip=0;
        c->current_mv_penalty= c->mv_penalty[s->f_code] + MAX_MV;
        fimin= interlaced_search(s, 0,
                                 s->b_field_mv_table[0], s->b_field_select_table[0],
                                 s->b_forw_mv_table[xy][0], s->b_forw_mv_table[xy][1], 0);
        c->current_mv_penalty= c->mv_penalty[s->b_code] + MAX_MV;
        bimin= interlaced_search(s, 2,
                                 s->b_field_mv_table[1], s->b_field_select_table[1],
                                 s->b_back_mv_table[xy][0], s->b_back_mv_table[xy][1], 0);
    }else
        fimin= bimin= INT_MAX;

    {
        int score= fmin;
        type = CANDIDATE_MB_TYPE_FORWARD;

        if (dmin <= score){
            score = dmin;
            type = CANDIDATE_MB_TYPE_DIRECT;
        }
        if(bmin<score){
            score=bmin;
            type= CANDIDATE_MB_TYPE_BACKWARD;
        }
        if(fbmin<score){
            score=fbmin;
            type= CANDIDATE_MB_TYPE_BIDIR;
        }
        if(fimin<score){
            score=fimin;
            type= CANDIDATE_MB_TYPE_FORWARD_I;
        }
        if(bimin<score){
            score=bimin;
            type= CANDIDATE_MB_TYPE_BACKWARD_I;
        }

        score= ((unsigned)(score*score + 128*256))>>16;
        c->mc_mb_var_sum_temp += score;
        s->current_picture.mc_mb_var[mb_y*s->mb_stride + mb_x] = score; //FIXME use SSE
    }

    if(c->avctx->mb_decision > FF_MB_DECISION_SIMPLE){
        type= CANDIDATE_MB_TYPE_FORWARD | CANDIDATE_MB_TYPE_BACKWARD | CANDIDATE_MB_TYPE_BIDIR | CANDIDATE_MB_TYPE_DIRECT;
        if(fimin < INT_MAX)
            type |= CANDIDATE_MB_TYPE_FORWARD_I;
        if(bimin < INT_MAX)
            type |= CANDIDATE_MB_TYPE_BACKWARD_I;
        if(fimin < INT_MAX && bimin < INT_MAX){
            type |= CANDIDATE_MB_TYPE_BIDIR_I;
        }
         //FIXME something smarter
        if(dmin>256*256*16) type&= ~CANDIDATE_MB_TYPE_DIRECT; //do not try direct mode if it is invalid for this MB
        if(s->codec_id == AV_CODEC_ID_MPEG4 && (type&CANDIDATE_MB_TYPE_DIRECT )&& ((s->flags&CODEC_FLAG_MV0) && *(uint32_t*)s->b_direct_mv_table[xy]))
            type |= CANDIDATE_MB_TYPE_DIRECT0;
    }

    s->mb_type[mb_y*s->mb_stride + mb_x]= type;
}

static void clip_input_mv(MpegEncContext * s, int16_t *mv, int interlaced){
    int ymax= s->me.ymax>>interlaced;
    int ymin= s->me.ymin>>interlaced;

    if(mv[0] < s->me.xmin) mv[0] = s->me.xmin;
    if(mv[0] > s->me.xmax) mv[0] = s->me.xmax;
    if(mv[1] <       ymin) mv[1] =       ymin;
    if(mv[1] >       ymax) mv[1] =       ymax;
}



static inline void init_mv4_ref(MotionEstContext *c){
    const int stride= c->stride;

    c->ref[1][0] = c->ref[0][0] + 8;
    c->ref[2][0] = c->ref[0][0] + 8*stride;
    c->ref[3][0] = c->ref[2][0] + 8;
    c->src[1][0] = c->src[0][0] + 8;
    c->src[2][0] = c->src[0][0] + 8*stride;
    c->src[3][0] = c->src[2][0] + 8;
}

inline int check_input_motion(MpegEncContext * s, int mb_x, int mb_y, int p_type){
    MotionEstContext * const c= &s->me;
    Picture *p= s->current_picture_ptr;
    int mb_xy= mb_x + mb_y*s->mb_stride;
    int xy= 2*mb_x + 2*mb_y*s->b8_stride;
    int mb_type= s->current_picture.f.mb_type[mb_xy];
    int flags= c->flags;
    int shift= (flags&FLAG_QPEL) + 1;
    int mask= (1<<shift)-1;
    int x, y, i;
    int d=0;
    me_cmp_func cmpf= s->dsp.sse[0];
    me_cmp_func chroma_cmpf= s->dsp.sse[1];

    if(p_type && USES_LIST(mb_type, 1)){
        printf( "backward motion vector in P frame\n");
        return INT_MAX/2;
    }
//    av_assert0(IS_INTRA(mb_type) || USES_LIST(mb_type,0) || USES_LIST(mb_type,1));

    for(i=0; i<4; i++){
        int xy= s->block_index[i];
        clip_input_mv(s, p->f.motion_val[0][xy], !!IS_INTERLACED(mb_type));
        clip_input_mv(s, p->f.motion_val[1][xy], !!IS_INTERLACED(mb_type));
    }

    if(IS_INTERLACED(mb_type)){
        int xy2= xy  + s->b8_stride;
        s->mb_type[mb_xy]=CANDIDATE_MB_TYPE_INTRA;
        c->stride<<=1;
        c->uvstride<<=1;

        if(!(s->flags & CODEC_FLAG_INTERLACED_ME)){
        	printf("Interlaced macroblock selected but interlaced motion estimation disabled\n");
            return INT_MAX/2;
        }

        if(USES_LIST(mb_type, 0)){
            int field_select0= p->f.ref_index[0][4*mb_xy  ];
            int field_select1= p->f.ref_index[0][4*mb_xy+2];
//            av_assert0(field_select0==0 ||field_select0==1);
//            av_assert0(field_select1==0 ||field_select1==1);
            init_interlaced_ref(s, 0);

            if(p_type){
                s->p_field_select_table[0][mb_xy]= field_select0;
                s->p_field_select_table[1][mb_xy]= field_select1;
                *(uint32_t*)s->p_field_mv_table[0][field_select0][mb_xy] = *(uint32_t*)p->f.motion_val[0][xy ];
                *(uint32_t*)s->p_field_mv_table[1][field_select1][mb_xy] = *(uint32_t*)p->f.motion_val[0][xy2];
                s->mb_type[mb_xy]=CANDIDATE_MB_TYPE_INTER_I;
            }else{
                s->b_field_select_table[0][0][mb_xy]= field_select0;
                s->b_field_select_table[0][1][mb_xy]= field_select1;
                *(uint32_t*)s->b_field_mv_table[0][0][field_select0][mb_xy] = *(uint32_t*)p->f.motion_val[0][xy ];
                *(uint32_t*)s->b_field_mv_table[0][1][field_select1][mb_xy] = *(uint32_t*)p->f.motion_val[0][xy2];
                s->mb_type[mb_xy]= CANDIDATE_MB_TYPE_FORWARD_I;
            }

            x = p->f.motion_val[0][xy ][0];
            y = p->f.motion_val[0][xy ][1];
            d = cmp(s, x>>shift, y>>shift, x&mask, y&mask, 0, 8, field_select0, 0, cmpf, chroma_cmpf, flags);
            x = p->f.motion_val[0][xy2][0];
            y = p->f.motion_val[0][xy2][1];
            d+= cmp(s, x>>shift, y>>shift, x&mask, y&mask, 0, 8, field_select1, 1, cmpf, chroma_cmpf, flags);
        }
        if(USES_LIST(mb_type, 1)){
            int field_select0 = p->f.ref_index[1][4 * mb_xy    ];
            int field_select1 = p->f.ref_index[1][4 * mb_xy + 2];
//            av_assert0(field_select0==0 ||field_select0==1);
//            av_assert0(field_select1==0 ||field_select1==1);
            init_interlaced_ref(s, 2);

            s->b_field_select_table[1][0][mb_xy]= field_select0;
            s->b_field_select_table[1][1][mb_xy]= field_select1;
            *(uint32_t*)s->b_field_mv_table[1][0][field_select0][mb_xy] = *(uint32_t*)p->f.motion_val[1][xy ];
            *(uint32_t*)s->b_field_mv_table[1][1][field_select1][mb_xy] = *(uint32_t*)p->f.motion_val[1][xy2];
            if(USES_LIST(mb_type, 0)){
                s->mb_type[mb_xy]= CANDIDATE_MB_TYPE_BIDIR_I;
            }else{
                s->mb_type[mb_xy]= CANDIDATE_MB_TYPE_BACKWARD_I;
            }

            x = p->f.motion_val[1][xy ][0];
            y = p->f.motion_val[1][xy ][1];
            d = cmp(s, x>>shift, y>>shift, x&mask, y&mask, 0, 8, field_select0+2, 0, cmpf, chroma_cmpf, flags);
            x = p->f.motion_val[1][xy2][0];
            y = p->f.motion_val[1][xy2][1];
            d+= cmp(s, x>>shift, y>>shift, x&mask, y&mask, 0, 8, field_select1+2, 1, cmpf, chroma_cmpf, flags);
            //FIXME bidir scores
        }
        c->stride>>=1;
        c->uvstride>>=1;
    }else if(IS_8X8(mb_type)){
        if(!(s->flags & CODEC_FLAG_4MV)){
            printf( "4MV macroblock selected but 4MV encoding disabled\n");
            return INT_MAX/2;
        }
        cmpf= s->dsp.sse[1];
        chroma_cmpf= s->dsp.sse[1];
        init_mv4_ref(c);
        for(i=0; i<4; i++){
            xy= s->block_index[i];
            x= p->f.motion_val[0][xy][0];
            y= p->f.motion_val[0][xy][1];
            d+= cmp(s, x>>shift, y>>shift, x&mask, y&mask, 1, 8, i, i, cmpf, chroma_cmpf, flags);
        }
        s->mb_type[mb_xy]=CANDIDATE_MB_TYPE_INTER4V;
    }else{
        if(USES_LIST(mb_type, 0)){
            if(p_type){
                *(uint32_t*)s->p_mv_table[mb_xy] = *(uint32_t*)p->f.motion_val[0][xy];
                s->mb_type[mb_xy]=CANDIDATE_MB_TYPE_INTER;
            }else if(USES_LIST(mb_type, 1)){
                *(uint32_t*)s->b_bidir_forw_mv_table[mb_xy] = *(uint32_t*)p->f.motion_val[0][xy];
                *(uint32_t*)s->b_bidir_back_mv_table[mb_xy] = *(uint32_t*)p->f.motion_val[1][xy];
                s->mb_type[mb_xy]=CANDIDATE_MB_TYPE_BIDIR;
            }else{
                *(uint32_t*)s->b_forw_mv_table[mb_xy] = *(uint32_t*)p->f.motion_val[0][xy];
                s->mb_type[mb_xy]=CANDIDATE_MB_TYPE_FORWARD;
            }
            x = p->f.motion_val[0][xy][0];
            y = p->f.motion_val[0][xy][1];
            d = cmp(s, x>>shift, y>>shift, x&mask, y&mask, 0, 16, 0, 0, cmpf, chroma_cmpf, flags);
        }else if(USES_LIST(mb_type, 1)){
            *(uint32_t*)s->b_back_mv_table[mb_xy] = *(uint32_t*)p->f.motion_val[1][xy];
            s->mb_type[mb_xy]=CANDIDATE_MB_TYPE_BACKWARD;

            x = p->f.motion_val[1][xy][0];
            y = p->f.motion_val[1][xy][1];
            d = cmp(s, x>>shift, y>>shift, x&mask, y&mask, 0, 16, 2, 0, cmpf, chroma_cmpf, flags);
        }else
            s->mb_type[mb_xy]=CANDIDATE_MB_TYPE_INTRA;
    }
    return d;
}


#   define ff_log2(x) (31 - __builtin_clz((x)|1))
inline unsigned int ff_sqrt(unsigned int a)
{
    unsigned int b;

    if (a < 255) return (ff_sqrt_tab[a + 1] - 1) >> 4;
    else if (a < (1 << 12)) b = ff_sqrt_tab[a >> 4] >> 2;
#if !CONFIG_SMALL
    else if (a < (1 << 14)) b = ff_sqrt_tab[a >> 6] >> 1;
    else if (a < (1 << 16)) b = ff_sqrt_tab[a >> 8]   ;
#endif
    else {
        int s = ff_log2(a >> 16) >> 1;
        unsigned int c = a >> (s + 2);
        b = ff_sqrt_tab[c >> (s + 8)];
        b = FASTDIV(c,b) + (b << s);
    }

    return b - (a < b * b);
}

inline void set_p_mv_tables(MpegEncContext * s, int mx, int my, int mv4)
{
    const int xy= s->mb_x + s->mb_y*s->mb_stride;

    s->p_mv_table[xy][0] = mx;
    s->p_mv_table[xy][1] = my;

    /* has already been set to the 4 MV if 4MV is done */
    if(mv4){
        int mot_xy= s->block_index[0];

        s->current_picture.f.motion_val[0][mot_xy    ][0] = mx;
        s->current_picture.f.motion_val[0][mot_xy    ][1] = my;
        s->current_picture.f.motion_val[0][mot_xy + 1][0] = mx;
        s->current_picture.f.motion_val[0][mot_xy + 1][1] = my;

        mot_xy += s->b8_stride;
        s->current_picture.f.motion_val[0][mot_xy    ][0] = mx;
        s->current_picture.f.motion_val[0][mot_xy    ][1] = my;
        s->current_picture.f.motion_val[0][mot_xy + 1][0] = mx;
        s->current_picture.f.motion_val[0][mot_xy + 1][1] = my;
    }
}

void ff_estimate_p_frame_motion(MpegEncContext * s,
                                int mb_x, int mb_y)
{
//	LOGD("ff_estimate_p_frame_motion P-FRAME Called quarter_sample:%d\n",s->quarter_sample);
    MotionEstContext * const c= &s->me;
    uint8_t *pix, *ppix;
    int sum, mx, my, dmin;
    int varc;            ///< the variance of the block (sum of squared (p[y][x]-average))
    int vard;            ///< sum of squared differences with the estimated motion vector
    int P[10][2];
    const int shift= 1+s->quarter_sample;
    int mb_type=0;
    Picture * const pic= &s->current_picture;

    init_ref(c, s->new_picture.f.data, s->last_picture.f.data, NULL, 16*mb_x, 16*mb_y, 0);

//    av_assert0(s->quarter_sample==0 || s->quarter_sample==1);
//    av_assert0(s->linesize == c->stride);
//    av_assert0(s->uvlinesize == c->uvstride);

    c->penalty_factor    = get_penalty_factor(s->lambda, s->lambda2, c->avctx->me_cmp);
    c->sub_penalty_factor= get_penalty_factor(s->lambda, s->lambda2, c->avctx->me_sub_cmp);
    c->mb_penalty_factor = get_penalty_factor(s->lambda, s->lambda2, c->avctx->mb_cmp);
    c->current_mv_penalty= c->mv_penalty[s->f_code] + MAX_MV;

//    LOGD("ff_estimate_p_frame_motion lambda:%d lambda2:%d stride:%d uvstride:%d me_cmp:%d me_sub_cmp:%d\n",s->lambda,s->lambda2,c->stride,c->uvstride,c->avctx->me_cmp,c->avctx->me_sub_cmp);
    get_limits(s, 16*mb_x, 16*mb_y);
    c->skip=0;

    /* intra / predictive decision */
    pix = c->src[0][0];
    sum = s->dsp.pix_sum(pix, s->linesize);
    varc = s->dsp.pix_norm1(pix, s->linesize) - (((unsigned)sum*sum)>>8) + 500;

    pic->mb_mean[s->mb_stride * mb_y + mb_x] = (sum+128)>>8;
    pic->mb_var [s->mb_stride * mb_y + mb_x] = (varc+128)>>8;
    c->mb_var_sum_temp += (varc+128)>>8;

    if(c->avctx->me_threshold){
        vard= check_input_motion(s, mb_x, mb_y, 1);

        if((vard+128)>>8 < c->avctx->me_threshold){
            int p_score= FFMIN(vard, varc-500+(s->lambda2>>FF_LAMBDA_SHIFT)*100);
            int i_score= varc-500+(s->lambda2>>FF_LAMBDA_SHIFT)*20;
            pic->mc_mb_var[s->mb_stride * mb_y + mb_x] = (vard+128)>>8;
            c->mc_mb_var_sum_temp += (vard+128)>>8;
            c->scene_change_score+= ff_sqrt(p_score) - ff_sqrt(i_score);
            return;
        }
        if((vard+128)>>8 < c->avctx->mb_threshold)
            mb_type= s->mb_type[mb_x + mb_y*s->mb_stride];
    }

    switch(s->me_method) {
    case ME_ZERO:
    default:
        mx   = 0;
        my   = 0;
        dmin = 0;
        break;
    case ME_X1:
    case ME_EPZS:
       {
            const int mot_stride = s->b8_stride;
            const int mot_xy = s->block_index[0];

            P_LEFT[0] = s->current_picture.f.motion_val[0][mot_xy - 1][0];
            P_LEFT[1] = s->current_picture.f.motion_val[0][mot_xy - 1][1];

            if(P_LEFT[0]       > (c->xmax<<shift)) P_LEFT[0]       = (c->xmax<<shift);

            if(!s->first_slice_line) {
                P_TOP[0]      = s->current_picture.f.motion_val[0][mot_xy - mot_stride    ][0];
                P_TOP[1]      = s->current_picture.f.motion_val[0][mot_xy - mot_stride    ][1];
                P_TOPRIGHT[0] = s->current_picture.f.motion_val[0][mot_xy - mot_stride + 2][0];
                P_TOPRIGHT[1] = s->current_picture.f.motion_val[0][mot_xy - mot_stride + 2][1];
                if(P_TOP[1]      > (c->ymax<<shift)) P_TOP[1]     = (c->ymax<<shift);
                if(P_TOPRIGHT[0] < (c->xmin<<shift)) P_TOPRIGHT[0]= (c->xmin<<shift);
                if(P_TOPRIGHT[1] > (c->ymax<<shift)) P_TOPRIGHT[1]= (c->ymax<<shift);

                P_MEDIAN[0]= mid_pred(P_LEFT[0], P_TOP[0], P_TOPRIGHT[0]);
                P_MEDIAN[1]= mid_pred(P_LEFT[1], P_TOP[1], P_TOPRIGHT[1]);

                if(s->out_format == FMT_H263){
                    c->pred_x = P_MEDIAN[0];
                    c->pred_y = P_MEDIAN[1];
                }else { /* mpeg1 at least */
                    c->pred_x= P_LEFT[0];
                    c->pred_y= P_LEFT[1];
                }
            }else{
                c->pred_x= P_LEFT[0];
                c->pred_y= P_LEFT[1];
            }

        }
        dmin = ff_epzs_motion_search(s, &mx, &my, P, 0, 0, s->p_mv_table, (1<<16)>>shift, 0, 16);
        break;
    }

    /* At this point (mx,my) are full-pell and the relative displacement */
    ppix = c->ref[0][0] + (my * s->linesize) + mx;

    vard = s->dsp.sse[0](NULL, pix, ppix, s->linesize, 16);

    pic->mc_mb_var[s->mb_stride * mb_y + mb_x] = (vard+128)>>8;
    c->mc_mb_var_sum_temp += (vard+128)>>8;

    if(mb_type){
        int p_score= FFMIN(vard, varc-500+(s->lambda2>>FF_LAMBDA_SHIFT)*100);
        int i_score= varc-500+(s->lambda2>>FF_LAMBDA_SHIFT)*20;
        c->scene_change_score+= ff_sqrt(p_score) - ff_sqrt(i_score);

        if(mb_type == CANDIDATE_MB_TYPE_INTER){
            c->sub_motion_search(s, &mx, &my, dmin, 0, 0, 0, 16);
            set_p_mv_tables(s, mx, my, 1);
        }else{
            mx <<=shift;
            my <<=shift;
        }
        if(mb_type == CANDIDATE_MB_TYPE_INTER4V){
            h263_mv4_search(s, mx, my, shift);

            set_p_mv_tables(s, mx, my, 0);
        }
        if(mb_type == CANDIDATE_MB_TYPE_INTER_I){
            interlaced_search(s, 0, s->p_field_mv_table, s->p_field_select_table, mx, my, 1);
        }
    }else if(c->avctx->mb_decision > FF_MB_DECISION_SIMPLE){
        int p_score= FFMIN(vard, varc-500+(s->lambda2>>FF_LAMBDA_SHIFT)*100);
        int i_score= varc-500+(s->lambda2>>FF_LAMBDA_SHIFT)*20;
        c->scene_change_score+= ff_sqrt(p_score) - ff_sqrt(i_score);

        if (vard*2 + 200*256 > varc)
            mb_type|= CANDIDATE_MB_TYPE_INTRA;
        if (varc*2 + 200*256 > vard || s->qscale > 24){
//        if (varc*2 + 200*256 + 50*(s->lambda2>>FF_LAMBDA_SHIFT) > vard){
            mb_type|= CANDIDATE_MB_TYPE_INTER;
            c->sub_motion_search(s, &mx, &my, dmin, 0, 0, 0, 16);
            if(s->flags&CODEC_FLAG_MV0)
                if(mx || my)
                    mb_type |= CANDIDATE_MB_TYPE_SKIPPED; //FIXME check difference
        }else{
            mx <<=shift;
            my <<=shift;
        }
        if((s->flags&CODEC_FLAG_4MV)
           && !c->skip && varc>50<<8 && vard>10<<8){
            if(h263_mv4_search(s, mx, my, shift) < INT_MAX)
                mb_type|=CANDIDATE_MB_TYPE_INTER4V;

            set_p_mv_tables(s, mx, my, 0);
        }else
            set_p_mv_tables(s, mx, my, 1);
        if((s->flags&CODEC_FLAG_INTERLACED_ME)
           && !c->skip){ //FIXME varc/d checks
            if(interlaced_search(s, 0, s->p_field_mv_table, s->p_field_select_table, mx, my, 0) < INT_MAX)
                mb_type |= CANDIDATE_MB_TYPE_INTER_I;
        }
    }else{
        int intra_score, i;
        mb_type= CANDIDATE_MB_TYPE_INTER;

        dmin= c->sub_motion_search(s, &mx, &my, dmin, 0, 0, 0, 16);
        if(c->avctx->me_sub_cmp != c->avctx->mb_cmp && !c->skip)
            dmin= get_mb_score(s, mx, my, 0, 0, 0, 16, 1);

        if((s->flags&CODEC_FLAG_4MV)
           && !c->skip && varc>50<<8 && vard>10<<8){
            int dmin4= h263_mv4_search(s, mx, my, shift);
            if(dmin4 < dmin){
                mb_type= CANDIDATE_MB_TYPE_INTER4V;
                dmin=dmin4;
            }
        }
        if((s->flags&CODEC_FLAG_INTERLACED_ME)
           && !c->skip){ //FIXME varc/d checks
            int dmin_i= interlaced_search(s, 0, s->p_field_mv_table, s->p_field_select_table, mx, my, 0);
            if(dmin_i < dmin){
                mb_type = CANDIDATE_MB_TYPE_INTER_I;
                dmin= dmin_i;
            }
        }

        set_p_mv_tables(s, mx, my, mb_type!=CANDIDATE_MB_TYPE_INTER4V);

        /* get intra luma score */
        if((c->avctx->mb_cmp&0xFF)==FF_CMP_SSE){
            intra_score= varc - 500;
        }else{
            unsigned mean = (sum+128)>>8;
            mean*= 0x01010101;

            for(i=0; i<16; i++){
                *(uint32_t*)(&c->scratchpad[i*s->linesize+ 0]) = mean;
                *(uint32_t*)(&c->scratchpad[i*s->linesize+ 4]) = mean;
                *(uint32_t*)(&c->scratchpad[i*s->linesize+ 8]) = mean;
                *(uint32_t*)(&c->scratchpad[i*s->linesize+12]) = mean;
            }

            intra_score= s->dsp.mb_cmp[0](s, c->scratchpad, pix, s->linesize, 16);
        }
        intra_score += c->mb_penalty_factor*16;

        if(intra_score < dmin){
            mb_type= CANDIDATE_MB_TYPE_INTRA;
            s->current_picture.f.mb_type[mb_y*s->mb_stride + mb_x] = CANDIDATE_MB_TYPE_INTRA; //FIXME cleanup
        }else
            s->current_picture.f.mb_type[mb_y*s->mb_stride + mb_x] = 0;

        {
            int p_score= FFMIN(vard, varc-500+(s->lambda2>>FF_LAMBDA_SHIFT)*100);
            int i_score= varc-500+(s->lambda2>>FF_LAMBDA_SHIFT)*20;
            c->scene_change_score+= ff_sqrt(p_score) - ff_sqrt(i_score);
        }
    }

    s->mb_type[mb_y*s->mb_stride + mb_x]= mb_type;
//	LOGD("ff_estimate_p_frame_motion END\n");
}

static int estimate_motion_thread(AVCodecContext *c, void *arg){
	LOGD("estimate_motion_thread Called\n");
    MpegEncContext *s= *(void**)arg;

    ff_check_alignment();

    s->me.dia_size= s->avctx->dia_size;
    s->first_slice_line=1;
    for(s->mb_y= s->start_mb_y; s->mb_y < s->end_mb_y; s->mb_y++) {
        s->mb_x=0; //for block init below
        ff_init_block_index(s);
        for(s->mb_x=0; s->mb_x < s->mb_width; s->mb_x++) {
            s->block_index[0]+=2;
            s->block_index[1]+=2;
            s->block_index[2]+=2;
            s->block_index[3]+=2;

            /* compute motion vector & mb_type and store in context */
            if(s->pict_type==AV_PICTURE_TYPE_B)
                ff_estimate_b_frame_motion(s, s->mb_x, s->mb_y);
            else
                ff_estimate_p_frame_motion(s, s->mb_x, s->mb_y);
        }
        s->first_slice_line=0;
    }
    return 0;
}







static int epzs_motion_search4(MpegEncContext * s,
                             int *mx_ptr, int *my_ptr, int P[10][2],
                             int src_index, int ref_index, int16_t (*last_mv)[2],
                             int ref_mv_scale)
{
    MotionEstContext * const c= &s->me;
    int best[2]={0, 0};
    int d, dmin;
    unsigned map_generation;
    const int penalty_factor= c->penalty_factor;
    const int size=1;
    const int h=8;
    const int ref_mv_stride= s->mb_stride;
    const int ref_mv_xy= s->mb_x + s->mb_y *ref_mv_stride;
    me_cmp_func cmpf, chroma_cmpf;
    LOAD_COMMON
    int flags= c->flags;
    LOAD_COMMON2

    cmpf= s->dsp.me_cmp[size];
    chroma_cmpf= s->dsp.me_cmp[size+1];

    map_generation= update_map_generation(c);

    dmin = 1000000;

    /* first line */
    if (s->first_slice_line) {
        CHECK_MV(P_LEFT[0]>>shift, P_LEFT[1]>>shift)
        CHECK_CLIPPED_MV((last_mv[ref_mv_xy][0]*ref_mv_scale + (1<<15))>>16,
                        (last_mv[ref_mv_xy][1]*ref_mv_scale + (1<<15))>>16)
        CHECK_MV(P_MV1[0]>>shift, P_MV1[1]>>shift)
    }else{
        CHECK_MV(P_MV1[0]>>shift, P_MV1[1]>>shift)
        //FIXME try some early stop
        CHECK_MV(P_MEDIAN[0]>>shift, P_MEDIAN[1]>>shift)
        CHECK_MV(P_LEFT[0]>>shift, P_LEFT[1]>>shift)
        CHECK_MV(P_TOP[0]>>shift, P_TOP[1]>>shift)
        CHECK_MV(P_TOPRIGHT[0]>>shift, P_TOPRIGHT[1]>>shift)
        CHECK_CLIPPED_MV((last_mv[ref_mv_xy][0]*ref_mv_scale + (1<<15))>>16,
                        (last_mv[ref_mv_xy][1]*ref_mv_scale + (1<<15))>>16)
    }
    if(dmin>64*4){
        CHECK_CLIPPED_MV((last_mv[ref_mv_xy+1][0]*ref_mv_scale + (1<<15))>>16,
                        (last_mv[ref_mv_xy+1][1]*ref_mv_scale + (1<<15))>>16)
        if(s->mb_y+1<s->end_mb_y)  //FIXME replace at least with last_slice_line
            CHECK_CLIPPED_MV((last_mv[ref_mv_xy+ref_mv_stride][0]*ref_mv_scale + (1<<15))>>16,
                            (last_mv[ref_mv_xy+ref_mv_stride][1]*ref_mv_scale + (1<<15))>>16)
    }

    dmin= diamond_search(s, best, dmin, src_index, ref_index, penalty_factor, size, h, flags);

    *mx_ptr= best[0];
    *my_ptr= best[1];

    return dmin;
}

static inline int ff_h263_round_chroma(int x){
    static const uint8_t h263_chroma_roundtab[16] = {
    //  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
        0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1,
    };
    return h263_chroma_roundtab[x & 0xf] + (x >> 3);
}

inline int h263_mv4_search(MpegEncContext *s, int mx, int my, int shift)
{
    MotionEstContext * const c= &s->me;
    const int size= 1;
    const int h=8;
    int block;
    int P[10][2];
    int dmin_sum=0, mx4_sum=0, my4_sum=0, i;
    int same=1;
    const int stride= c->stride;
    uint8_t *mv_penalty= c->current_mv_penalty;
    int saftey_cliping= s->unrestricted_mv && (s->width&15) && (s->height&15);

    init_mv4_ref(c);

    for(block=0; block<4; block++){
        int mx4, my4;
        int pred_x4, pred_y4;
        int dmin4;
        static const int off[4]= {2, 1, 1, -1};
        const int mot_stride = s->b8_stride;
        const int mot_xy = s->block_index[block];

        if(saftey_cliping){
            c->xmax = - 16*s->mb_x + s->width  - 8*(block &1);
            c->ymax = - 16*s->mb_y + s->height - 8*(block>>1);
        }

        P_LEFT[0] = s->current_picture.f.motion_val[0][mot_xy - 1][0];
        P_LEFT[1] = s->current_picture.f.motion_val[0][mot_xy - 1][1];

        if(P_LEFT[0]       > (c->xmax<<shift)) P_LEFT[0]       = (c->xmax<<shift);

        /* special case for first line */
        if (s->first_slice_line && block<2) {
            c->pred_x= pred_x4= P_LEFT[0];
            c->pred_y= pred_y4= P_LEFT[1];
        } else {
            P_TOP[0]      = s->current_picture.f.motion_val[0][mot_xy - mot_stride             ][0];
            P_TOP[1]      = s->current_picture.f.motion_val[0][mot_xy - mot_stride             ][1];
            P_TOPRIGHT[0] = s->current_picture.f.motion_val[0][mot_xy - mot_stride + off[block]][0];
            P_TOPRIGHT[1] = s->current_picture.f.motion_val[0][mot_xy - mot_stride + off[block]][1];
            if(P_TOP[1]      > (c->ymax<<shift)) P_TOP[1]     = (c->ymax<<shift);
            if(P_TOPRIGHT[0] < (c->xmin<<shift)) P_TOPRIGHT[0]= (c->xmin<<shift);
            if(P_TOPRIGHT[0] > (c->xmax<<shift)) P_TOPRIGHT[0]= (c->xmax<<shift);
            if(P_TOPRIGHT[1] > (c->ymax<<shift)) P_TOPRIGHT[1]= (c->ymax<<shift);

            P_MEDIAN[0]= mid_pred(P_LEFT[0], P_TOP[0], P_TOPRIGHT[0]);
            P_MEDIAN[1]= mid_pred(P_LEFT[1], P_TOP[1], P_TOPRIGHT[1]);

            c->pred_x= pred_x4 = P_MEDIAN[0];
            c->pred_y= pred_y4 = P_MEDIAN[1];
        }
        P_MV1[0]= mx;
        P_MV1[1]= my;
        if(saftey_cliping)
            for(i=0; i<10; i++){
                if(P[i][0] > (c->xmax<<shift)) P[i][0]= (c->xmax<<shift);
                if(P[i][1] > (c->ymax<<shift)) P[i][1]= (c->ymax<<shift);
            }

        dmin4 = epzs_motion_search4(s, &mx4, &my4, P, block, block, s->p_mv_table, (1<<16)>>shift);

        dmin4= c->sub_motion_search(s, &mx4, &my4, dmin4, block, block, size, h);

        if(s->dsp.me_sub_cmp[0] != s->dsp.mb_cmp[0]){
            int dxy;
            const int offset= ((block&1) + (block>>1)*stride)*8;
            uint8_t *dest_y = c->scratchpad + offset;
            if(s->quarter_sample){
                uint8_t *ref= c->ref[block][0] + (mx4>>2) + (my4>>2)*stride;
                dxy = ((my4 & 3) << 2) | (mx4 & 3);

                if(s->no_rounding)
                    s->dsp.put_no_rnd_qpel_pixels_tab[1][dxy](dest_y   , ref    , stride);
                else
                    s->dsp.put_qpel_pixels_tab       [1][dxy](dest_y   , ref    , stride);
            }else{
                uint8_t *ref= c->ref[block][0] + (mx4>>1) + (my4>>1)*stride;
                dxy = ((my4 & 1) << 1) | (mx4 & 1);

                if(s->no_rounding)
                    s->dsp.put_no_rnd_pixels_tab[1][dxy](dest_y    , ref    , stride, h);
                else
                    s->dsp.put_pixels_tab       [1][dxy](dest_y    , ref    , stride, h);
            }
            dmin_sum+= (mv_penalty[mx4-pred_x4] + mv_penalty[my4-pred_y4])*c->mb_penalty_factor;
        }else
            dmin_sum+= dmin4;

        if(s->quarter_sample){
            mx4_sum+= mx4/2;
            my4_sum+= my4/2;
        }else{
            mx4_sum+= mx4;
            my4_sum+= my4;
        }

        s->current_picture.f.motion_val[0][s->block_index[block]][0] = mx4;
        s->current_picture.f.motion_val[0][s->block_index[block]][1] = my4;

        if(mx4 != mx || my4 != my) same=0;
    }

    if(same)
        return INT_MAX;

    if(s->dsp.me_sub_cmp[0] != s->dsp.mb_cmp[0]){
        dmin_sum += s->dsp.mb_cmp[0](s, s->new_picture.f.data[0] + s->mb_x*16 + s->mb_y*16*stride, c->scratchpad, stride, 16);
    }

    if(c->avctx->mb_cmp&FF_CMP_CHROMA){
        int dxy;
        int mx, my;
        int offset;

        mx= ff_h263_round_chroma(mx4_sum);
        my= ff_h263_round_chroma(my4_sum);
        dxy = ((my & 1) << 1) | (mx & 1);

        offset= (s->mb_x*8 + (mx>>1)) + (s->mb_y*8 + (my>>1))*s->uvlinesize;

        if(s->no_rounding){
            s->dsp.put_no_rnd_pixels_tab[1][dxy](c->scratchpad    , s->last_picture.f.data[1] + offset, s->uvlinesize, 8);
            s->dsp.put_no_rnd_pixels_tab[1][dxy](c->scratchpad + 8, s->last_picture.f.data[2] + offset, s->uvlinesize, 8);
        }else{
            s->dsp.put_pixels_tab       [1][dxy](c->scratchpad    , s->last_picture.f.data[1] + offset, s->uvlinesize, 8);
            s->dsp.put_pixels_tab       [1][dxy](c->scratchpad + 8, s->last_picture.f.data[2] + offset, s->uvlinesize, 8);
        }

        dmin_sum += s->dsp.mb_cmp[1](s, s->new_picture.f.data[1] + s->mb_x*8 + s->mb_y*8*s->uvlinesize, c->scratchpad  , s->uvlinesize, 8);
        dmin_sum += s->dsp.mb_cmp[1](s, s->new_picture.f.data[2] + s->mb_x*8 + s->mb_y*8*s->uvlinesize, c->scratchpad+8, s->uvlinesize, 8);
    }

    c->pred_x= mx;
    c->pred_y= my;

    switch(c->avctx->mb_cmp&0xFF){
    /*case FF_CMP_SSE:
        return dmin_sum+ 32*s->qscale*s->qscale;*/
    case FF_CMP_RD:
        return dmin_sum;
    default:
        return dmin_sum+ 11*c->mb_penalty_factor;
    }
}




static int mb_var_thread(AVCodecContext *c, void *arg){
    MpegEncContext *s= *(void**)arg;
    int mb_x, mb_y;
//    LOGD("mb_var_thread Called\n");
    ff_check_alignment();
//	LOGD("mb_var_thread linesize:%d starty:%d end_y:%d mb_width:%d\n",s->linesize,s->start_mb_y,s->end_mb_y,s->mb_width);
//	LOGD("mb_var_thread VIDEO_INPUT_DEBUG frame:%d\n",s->new_picture.f.data[0]);
//			if(s->new_picture.f.data[0][0]){//普通にYUVが入っている
//				int i;
//				LOGD("INPUT s->new_picture.f.data[0][0]:%d\n",s->new_picture.f.data[0][0]);
//				for(i = 0; i < 20/*480*320*3/2でもいけた*/; i++){
//					LOGD("%02x ",s->new_picture.f.data[0][i]);
//				}
//				LOGD("\n");
//			}
    for(mb_y=s->start_mb_y; mb_y < s->end_mb_y; mb_y++) {
        for(mb_x=0; mb_x < s->mb_width; mb_x++) {
            int xx = mb_x * 16;
            int yy = mb_y * 16;
            uint8_t *pix = s->new_picture.f.data[0] + (yy * s->linesize) + xx;//相対的なメモリ位置
            int varc;

            int sum = s->dsp.pix_sum(pix, s->linesize);

            varc = (s->dsp.pix_norm1(pix, s->linesize) - (((unsigned)sum*sum)>>8) + 500 + 128)>>8;

//            LOGD("mb_var_thread sum%d varc:%d\n",sum,varc);
            s->current_picture.mb_var [s->mb_stride * mb_y + mb_x] = varc;
            s->current_picture.mb_mean[s->mb_stride * mb_y + mb_x] = (sum+128)>>8;
            s->me.mb_var_sum_temp    += varc;
//            LOGD("yy:%d s->linesize:%d xx:%d\n",yy,s->linesize,xx);
//            LOGD("data:%02x   sum:%d varc:%d mb_var_sum_temp:%d\n",*(s->new_picture.f.data[0]+ (yy * s->linesize) + xx),sum,varc,s->me.mb_var_sum_temp);
        }
    }
//    LOGD("mb_var_thread END mb_var_sum_temp:%d\n", s->me.mb_var_sum_temp );

    return 0;
}



/**
 * Return the pointer to the byte where the bitstream writer will put
 * the next bit.
 */
static inline uint8_t *put_bits_ptr(PutBitContext *s)
{
    return s->buf_ptr;
}


/**
 * set qscale and update qscale dependent variables.
 */
void ff_set_qscale(MpegEncContext * s, int qscale)
{
    if (qscale < 1)
        qscale = 1;
    else if (qscale > 31)
        qscale = 31;

    s->qscale = qscale;
    s->chroma_qscale= s->chroma_qscale_table[qscale];

    s->y_dc_scale= s->y_dc_scale_table[ qscale ];
    s->c_dc_scale= s->c_dc_scale_table[ s->chroma_qscale ];
}


/**
 * Change the end of the buffer.
 *
 * @param size the new size in bytes of the buffer where to put bits
 */
static inline void set_put_bits_buffer_size(PutBitContext *s, int size)
{
    s->buf_end = s->buf + size;
}

void ff_mpeg4_init_partitions(MpegEncContext *s)
{
    uint8_t *start= put_bits_ptr(&s->pb);
    uint8_t *end= s->pb.buf_end;
    int size= end - start;
    int pb_size = (((intptr_t)start + size/3)&(~3)) - (intptr_t)start;
    int tex_size= (size - 2*pb_size)&(~3);

    set_put_bits_buffer_size(&s->pb, pb_size);
    init_put_bits(&s->tex_pb, start + pb_size           , tex_size);
    init_put_bits(&s->pb2   , start + pb_size + tex_size, pb_size);
}


/**
 * @return the total number of bits written to the bitstream.
 */
static inline int put_bits_count(PutBitContext *s)
{
    return (s->buf_ptr - s->buf) * 8 + 32 - s->bit_left;
}

/**
 * Get the GOB height based on picture height.
 */
int ff_h263_get_gob_height(MpegEncContext *s){
    if (s->height <= 400)
        return 1;
    else if (s->height <= 800)
        return  2;
    else
        return 4;
}

static inline void ff_update_block_index(MpegEncContext *s){
    const int block_size= 8 >> s->avctx->lowres;

    s->block_index[0]+=2;
    s->block_index[1]+=2;
    s->block_index[2]+=2;
    s->block_index[3]+=2;
    s->block_index[4]++;
    s->block_index[5]++;
    s->dest[0]+= 2*block_size;
    s->dest[1]+= block_size;
    s->dest[2]+= block_size;
}


int16_t *ff_h263_pred_motion(MpegEncContext * s, int block, int dir,
                             int *px, int *py)
{
    int wrap;
    int16_t *A, *B, *C, (*mot_val)[2];
    static const int off[4]= {2, 1, 1, -1};

    wrap = s->b8_stride;
    mot_val = s->current_picture.f.motion_val[dir] + s->block_index[block];

    A = mot_val[ - 1];
    /* special case for first (slice) line */
    if (s->first_slice_line && block<3) {
        // we can't just change some MVs to simulate that as we need them for the B frames (and ME)
        // and if we ever support non rectangular objects than we need to do a few ifs here anyway :(
        if(block==0){ //most common case
            if(s->mb_x  == s->resync_mb_x){ //rare
                *px= *py = 0;
            }else if(s->mb_x + 1 == s->resync_mb_x && s->h263_pred){ //rare
                C = mot_val[off[block] - wrap];
                if(s->mb_x==0){
                    *px = C[0];
                    *py = C[1];
                }else{
                    *px = mid_pred(A[0], 0, C[0]);
                    *py = mid_pred(A[1], 0, C[1]);
                }
            }else{
                *px = A[0];
                *py = A[1];
            }
        }else if(block==1){
            if(s->mb_x + 1 == s->resync_mb_x && s->h263_pred){ //rare
                C = mot_val[off[block] - wrap];
                *px = mid_pred(A[0], 0, C[0]);
                *py = mid_pred(A[1], 0, C[1]);
            }else{
                *px = A[0];
                *py = A[1];
            }
        }else{ /* block==2*/
            B = mot_val[ - wrap];
            C = mot_val[off[block] - wrap];
            if(s->mb_x == s->resync_mb_x) //rare
                A[0]=A[1]=0;

            *px = mid_pred(A[0], B[0], C[0]);
            *py = mid_pred(A[1], B[1], C[1]);
        }
    } else {
        B = mot_val[ - wrap];
        C = mot_val[off[block] - wrap];
        *px = mid_pred(A[0], B[0], C[0]);
        *py = mid_pred(A[1], B[1], C[1]);
    }
    return *mot_val;
}


static void write_mb_info(MpegEncContext *s)
{
    uint8_t *ptr = s->mb_info_ptr + s->mb_info_size - 12;
    int offset = put_bits_count(&s->pb);
    int mba  = s->mb_x + s->mb_width * (s->mb_y % s->gob_index);
    int gobn = s->mb_y / s->gob_index;
    int pred_x, pred_y;
    if (CONFIG_H263_ENCODER)
        ff_h263_pred_motion(s, 0, 0, &pred_x, &pred_y);
    bytestream_put_le32(&ptr, offset);
    bytestream_put_byte(&ptr, s->qscale);
    bytestream_put_byte(&ptr, gobn);
    bytestream_put_le16(&ptr, mba);
    bytestream_put_byte(&ptr, pred_x); /* hmv1 */
    bytestream_put_byte(&ptr, pred_y); /* vmv1 */
    /* 4MV not implemented */
    bytestream_put_byte(&ptr, 0); /* hmv2 */
    bytestream_put_byte(&ptr, 0); /* vmv2 */
}

static void update_mb_info(MpegEncContext *s, int startcode)
{
    if (!s->mb_info)
        return;
    if (put_bits_count(&s->pb) - s->prev_mb_info*8 >= s->mb_info*8) {
        s->mb_info_size += 12;
        s->prev_mb_info = s->last_mb_info;
    }
    if (startcode) {
        s->prev_mb_info = put_bits_count(&s->pb)/8;
        /* This might have incremented mb_info_size above, and we return without
         * actually writing any info into that slot yet. But in that case,
         * this will be called again at the start of the after writing the
         * start code, actually writing the mb info. */
        return;
    }

    s->last_mb_info = put_bits_count(&s->pb)/8;
    if (!s->mb_info_size)
        s->mb_info_size += 12;
    write_mb_info(s);
}


/**
 * Pad the end of the output stream with zeros.
 */
static inline void flush_put_bits(PutBitContext *s)
{
#ifndef BITSTREAM_WRITER_LE
    if (s->bit_left < 32)
        s->bit_buf <<= s->bit_left;
#endif
    while (s->bit_left < 32) {
        /* XXX: should test end of buffer */
#ifdef BITSTREAM_WRITER_LE
        *s->buf_ptr++ = s->bit_buf;
        s->bit_buf  >>= 8;
#else
        *s->buf_ptr++ = s->bit_buf >> 24;
        s->bit_buf  <<= 8;
#endif
        s->bit_left  += 8;
    }
    s->bit_left = 32;
    s->bit_buf  = 0;
}


/**
 * Skip the given number of bytes.
 * PutBitContext must be flushed & aligned to a byte boundary before calling this.
 */
static inline void skip_put_bytes(PutBitContext *s, int n)
{
//    av_assert2((put_bits_count(s) & 7) == 0);
//    av_assert2(s->bit_left == 32);
    s->buf_ptr += n;
}


void avpriv_copy_bits(PutBitContext *pb, const uint8_t *src, int length)
{
	LOGD("avpriv_copy_bits Called\n");
    int words= length>>4;
//    int bits= length&15;
    int i;

    if(length==0) return;

    if(CONFIG_SMALL || words < 16 || (put_bits_count(pb)&7)){
    	//マクロでエラーして手におえない matching constraint references invalid operand number
//        for(i=0; i<words; i++) put_bits(pb, 16, AV_RB16(src + 2*i));
    }else{
        for(i=0; put_bits_count(pb)&31; i++)
            put_bits(pb, 8, src[i]);
        flush_put_bits(pb);
        memcpy(put_bits_ptr(pb), src+i, 2*words-i);
        skip_put_bytes(pb, 2*words-i);
    }
    //コピーを要実装
//    put_bits(pb, bits, AV_RB16(src + 2*words)>>(16-bits));
}

void ff_mpeg4_merge_partitions(MpegEncContext *s)
{
    const int pb2_len   = put_bits_count(&s->pb2   );
    const int tex_pb_len= put_bits_count(&s->tex_pb);
    const int bits= put_bits_count(&s->pb);

    if(s->pict_type==AV_PICTURE_TYPE_I){
        put_bits(&s->pb, 19, DC_MARKER);
        s->misc_bits+=19 + pb2_len + bits - s->last_bits;
        s->i_tex_bits+= tex_pb_len;
    }else{
        put_bits(&s->pb, 17, MOTION_MARKER);
        s->misc_bits+=17 + pb2_len;
        s->mv_bits+= bits - s->last_bits;
        s->p_tex_bits+= tex_pb_len;
    }

    flush_put_bits(&s->pb2);
    flush_put_bits(&s->tex_pb);

    set_put_bits_buffer_size(&s->pb, s->pb2.buf_end - s->pb.buf);
    avpriv_copy_bits(&s->pb, s->pb2.buf   , pb2_len);
    avpriv_copy_bits(&s->pb, s->tex_pb.buf, tex_pb_len);
    s->last_bits= put_bits_count(&s->pb);
}


/**
 * add mpeg4 stuffing bits (01...1)
 */
void ff_mpeg4_stuffing(PutBitContext * pbc)
{
    int length;
    put_bits(pbc, 1, 0);
    length= (-put_bits_count(pbc))&7;
    if(length) put_bits(pbc, length, (1<<length)-1);
}


void avpriv_align_put_bits(PutBitContext *s)
{
    put_bits(s,s->bit_left & 7,0);
}

static void escape_FF(MpegEncContext *s, int start)
{
    int size= put_bits_count(&s->pb) - start*8;
    int i, ff_count;
    uint8_t *buf= s->pb.buf + start;
    int align= (-(size_t)(buf))&3;

//    av_assert1((size&7) == 0);
    size >>= 3;

    ff_count=0;
    for(i=0; i<size && i<align; i++){
        if(buf[i]==0xFF) ff_count++;
    }
    for(; i<size-15; i+=16){
        int acc, v;

        v= *(uint32_t*)(&buf[i]);
        acc= (((v & (v>>4))&0x0F0F0F0F)+0x01010101)&0x10101010;
        v= *(uint32_t*)(&buf[i+4]);
        acc+=(((v & (v>>4))&0x0F0F0F0F)+0x01010101)&0x10101010;
        v= *(uint32_t*)(&buf[i+8]);
        acc+=(((v & (v>>4))&0x0F0F0F0F)+0x01010101)&0x10101010;
        v= *(uint32_t*)(&buf[i+12]);
        acc+=(((v & (v>>4))&0x0F0F0F0F)+0x01010101)&0x10101010;

        acc>>=4;
        acc+= (acc>>16);
        acc+= (acc>>8);
        ff_count+= acc&0xFF;
    }
    for(; i<size; i++){
        if(buf[i]==0xFF) ff_count++;
    }

    if(ff_count==0) return;

    flush_put_bits(&s->pb);
    skip_put_bytes(&s->pb, ff_count);

    for(i=size-1; ff_count; i--){
        int v= buf[i];

        if(v==0xFF){
            buf[i+ff_count]= 0;
            ff_count--;
        }

        buf[i+ff_count]= v;
    }
}

void ff_mjpeg_encode_stuffing(MpegEncContext *s)
{
    int length, i;
    PutBitContext *pbc = &s->pb;
    int mb_y = s->mb_y - !s->mb_x;
    length= (-put_bits_count(pbc))&7;
    if(length) put_bits(pbc, length, (1<<length)-1);

    flush_put_bits(&s->pb);
    escape_FF(s, s->esc_pos);

    if((s->avctx->active_thread_type & FF_THREAD_SLICE) && mb_y < s->mb_height)
        put_marker(pbc, RST0 + (mb_y&7));
    s->esc_pos = put_bits_count(pbc) >> 3;

    for(i=0; i<3; i++)
        s->last_dc[i] = 128 << s->intra_dc_precision;
}


static inline int get_bits_diff(MpegEncContext *s){
    const int bits= put_bits_count(&s->pb);
    const int last= s->last_bits;

    s->last_bits = bits;

    return bits - last;
}

static void write_slice_end(MpegEncContext *s){
    if(CONFIG_MPEG4_ENCODER && s->codec_id==AV_CODEC_ID_MPEG4){
        if(s->partitioned_frame){
            ff_mpeg4_merge_partitions(s);
        }

        ff_mpeg4_stuffing(&s->pb);
    }else if(CONFIG_MJPEG_ENCODER && s->out_format == FMT_MJPEG){
        ff_mjpeg_encode_stuffing(s);
    }

    avpriv_align_put_bits(&s->pb);
    flush_put_bits(&s->pb);

    if((s->flags&CODEC_FLAG_PASS1) && !s->partitioned_frame)
        s->misc_bits+= get_bits_diff(s);
}

int ff_mpeg4_get_video_packet_prefix_length(MpegEncContext *s){
    switch(s->pict_type){
        case AV_PICTURE_TYPE_I:
            return 16;
        case AV_PICTURE_TYPE_P:
        case AV_PICTURE_TYPE_S:
            return s->f_code+15;
        case AV_PICTURE_TYPE_B:
            return FFMAX3(s->f_code, s->b_code, 2) + 15;
        default:
            return -1;
    }
}

void ff_mpeg4_encode_video_packet_header(MpegEncContext *s)
{
    int mb_num_bits= av_log2(s->mb_num - 1) + 1;

    put_bits(&s->pb, ff_mpeg4_get_video_packet_prefix_length(s), 0);
    put_bits(&s->pb, 1, 1);

    put_bits(&s->pb, mb_num_bits, s->mb_x + s->mb_y*s->mb_width);
    put_bits(&s->pb, s->quant_precision, s->qscale);
    put_bits(&s->pb, 1, 0); /* no HEC */
}


void ff_mpeg4_clean_buffers(MpegEncContext *s)
{
    int c_wrap, c_xy, l_wrap, l_xy;

    l_wrap= s->b8_stride;
    l_xy= (2*s->mb_y-1)*l_wrap + s->mb_x*2 - 1;
    c_wrap= s->mb_stride;
    c_xy= (s->mb_y-1)*c_wrap + s->mb_x - 1;

#if 0
    /* clean DC */
    memsetw(s->dc_val[0] + l_xy, 1024, l_wrap*2+1);
    memsetw(s->dc_val[1] + c_xy, 1024, c_wrap+1);
    memsetw(s->dc_val[2] + c_xy, 1024, c_wrap+1);
#endif

    /* clean AC */
    memset(s->ac_val[0] + l_xy, 0, (l_wrap*2+1)*16*sizeof(int16_t));
    memset(s->ac_val[1] + c_xy, 0, (c_wrap  +1)*16*sizeof(int16_t));
    memset(s->ac_val[2] + c_xy, 0, (c_wrap  +1)*16*sizeof(int16_t));

    /* clean MV */
    // we can't clear the MVs as they might be needed by a b frame
//    memset(s->motion_val + l_xy, 0, (l_wrap*2+1)*2*sizeof(int16_t));
//    memset(s->motion_val, 0, 2*sizeof(int16_t)*(2 + s->mb_width*2)*(2 + s->mb_height*2));
    s->last_mv[0][0][0]=
    s->last_mv[0][0][1]=
    s->last_mv[1][0][0]=
    s->last_mv[1][0][1]= 0;
}


void ff_h263_encode_mba(MpegEncContext *s)
{
    int i, mb_pos;

    for(i=0; i<6; i++){
        if(s->mb_num-1 <= ff_mba_max[i]) break;
    }
    mb_pos= s->mb_x + s->mb_width*s->mb_y;
    put_bits(&s->pb, ff_mba_length[i], mb_pos);
}

/**
 * Encode a group of blocks header.
 */
void ff_h263_encode_gob_header(MpegEncContext * s, int mb_line)
{
    put_bits(&s->pb, 17, 1); /* GBSC */

    if(s->h263_slice_structured){
        put_bits(&s->pb, 1, 1);

        ff_h263_encode_mba(s);

        if(s->mb_num > 1583)
            put_bits(&s->pb, 1, 1);
        put_bits(&s->pb, 5, s->qscale); /* GQUANT */
        put_bits(&s->pb, 1, 1);
        put_bits(&s->pb, 2, s->pict_type == AV_PICTURE_TYPE_I); /* GFID */
    }else{
        int gob_number= mb_line / s->gob_index;

        put_bits(&s->pb, 5, gob_number); /* GN */
        put_bits(&s->pb, 2, s->pict_type == AV_PICTURE_TYPE_I); /* GFID */
        put_bits(&s->pb, 5, s->qscale); /* GQUANT */
    }
}

static inline void copy_context_before_encode(MpegEncContext *d, MpegEncContext *s, int type){
    int i;

    memcpy(d->last_mv, s->last_mv, 2*2*2*sizeof(int)); //FIXME is memcpy faster than a loop?

    /* mpeg1 */
    d->mb_skip_run= s->mb_skip_run;
    for(i=0; i<3; i++)
        d->last_dc[i] = s->last_dc[i];

    /* statistics */
    d->mv_bits= s->mv_bits;
    d->i_tex_bits= s->i_tex_bits;
    d->p_tex_bits= s->p_tex_bits;
    d->i_count= s->i_count;
    d->f_count= s->f_count;
    d->b_count= s->b_count;
    d->skip_count= s->skip_count;
    d->misc_bits= s->misc_bits;
    d->last_bits= 0;

    d->mb_skipped= 0;
    d->qscale= s->qscale;
    d->dquant= s->dquant;

    d->esc3_level_length= s->esc3_level_length;
}


static inline void update_qscale(MpegEncContext *s)
{
	s->qscale = (s->lambda * 139 + FF_LAMBDA_SCALE * 64) >>
                (FF_LAMBDA_SHIFT + 7);
    LOGD("update_qscale lambda:%d SCALE:%d SHIFT:%d qscale:%d\n",s->lambda,FF_LAMBDA_SCALE,FF_LAMBDA_SHIFT,s->qscale);
    s->qscale = av_clip_c(s->qscale, s->avctx->qmin, s->avctx->qmax);

    s->lambda2 = (s->lambda * s->lambda + FF_LAMBDA_SCALE / 2) >>
                 FF_LAMBDA_SHIFT;
    LOGD("update_qscale lambda:%d SCALE:%d SHIFT:%d qscale:%d\n",s->lambda,FF_LAMBDA_SCALE,FF_LAMBDA_SHIFT,s->qscale);
    LOGD("update_qscale qscale:%d q->lambda:%d q->lambda2:%d\n",s->qscale,s->lambda,s->lambda2);
}

static inline int hpel_motion(MpegEncContext *s,
                                  uint8_t *dest, uint8_t *src,
                                  int src_x, int src_y,
                                  op_pixels_func *pix_op,
                                  int motion_x, int motion_y)
{
    int dxy = 0;
    int emu=0;

    src_x += motion_x >> 1;
    src_y += motion_y >> 1;

    /* WARNING: do no forget half pels */
    src_x = av_clip_c(src_x, -16, s->width); //FIXME unneeded for emu?
    if (src_x != s->width)
        dxy |= motion_x & 1;
    src_y = av_clip_c(src_y, -16, s->height);
    if (src_y != s->height)
        dxy |= (motion_y & 1) << 1;
    src += src_y * s->linesize + src_x;

    if(s->unrestricted_mv && (s->flags&CODEC_FLAG_EMU_EDGE)){
        if(   (unsigned)src_x > FFMAX(s->h_edge_pos - (motion_x&1) - 8, 0)
           || (unsigned)src_y > FFMAX(s->v_edge_pos - (motion_y&1) - 8, 0)){
            s->vdsp.emulated_edge_mc(s->edge_emu_buffer, src, s->linesize, 9, 9,
                             src_x, src_y, s->h_edge_pos, s->v_edge_pos);
            src= s->edge_emu_buffer;
            emu=1;
        }
    }
    pix_op[dxy](dest, src, s->linesize, 8);
    return emu;
}



//FIXME move to dsputil, avg variant, 16x16 version
static inline void put_obmc(uint8_t *dst, uint8_t *src[5], int stride){
    int x;
    uint8_t * const top   = src[1];
    uint8_t * const left  = src[2];
    uint8_t * const mid   = src[0];
    uint8_t * const right = src[3];
    uint8_t * const bottom= src[4];
#define OBMC_FILTER(x, t, l, m, r, b)\
    dst[x]= (t*top[x] + l*left[x] + m*mid[x] + r*right[x] + b*bottom[x] + 4)>>3
#define OBMC_FILTER4(x, t, l, m, r, b)\
    OBMC_FILTER(x         , t, l, m, r, b);\
    OBMC_FILTER(x+1       , t, l, m, r, b);\
    OBMC_FILTER(x  +stride, t, l, m, r, b);\
    OBMC_FILTER(x+1+stride, t, l, m, r, b);

    x=0;
    OBMC_FILTER (x  , 2, 2, 4, 0, 0);
    OBMC_FILTER (x+1, 2, 1, 5, 0, 0);
    OBMC_FILTER4(x+2, 2, 1, 5, 0, 0);
    OBMC_FILTER4(x+4, 2, 0, 5, 1, 0);
    OBMC_FILTER (x+6, 2, 0, 5, 1, 0);
    OBMC_FILTER (x+7, 2, 0, 4, 2, 0);
    x+= stride;
    OBMC_FILTER (x  , 1, 2, 5, 0, 0);
    OBMC_FILTER (x+1, 1, 2, 5, 0, 0);
    OBMC_FILTER (x+6, 1, 0, 5, 2, 0);
    OBMC_FILTER (x+7, 1, 0, 5, 2, 0);
    x+= stride;
    OBMC_FILTER4(x  , 1, 2, 5, 0, 0);
    OBMC_FILTER4(x+2, 1, 1, 6, 0, 0);
    OBMC_FILTER4(x+4, 1, 0, 6, 1, 0);
    OBMC_FILTER4(x+6, 1, 0, 5, 2, 0);
    x+= 2*stride;
    OBMC_FILTER4(x  , 0, 2, 5, 0, 1);
    OBMC_FILTER4(x+2, 0, 1, 6, 0, 1);
    OBMC_FILTER4(x+4, 0, 0, 6, 1, 1);
    OBMC_FILTER4(x+6, 0, 0, 5, 2, 1);
    x+= 2*stride;
    OBMC_FILTER (x  , 0, 2, 5, 0, 1);
    OBMC_FILTER (x+1, 0, 2, 5, 0, 1);
    OBMC_FILTER4(x+2, 0, 1, 5, 0, 2);
    OBMC_FILTER4(x+4, 0, 0, 5, 1, 2);
    OBMC_FILTER (x+6, 0, 0, 5, 2, 1);
    OBMC_FILTER (x+7, 0, 0, 5, 2, 1);
    x+= stride;
    OBMC_FILTER (x  , 0, 2, 4, 0, 2);
    OBMC_FILTER (x+1, 0, 1, 5, 0, 2);
    OBMC_FILTER (x+6, 0, 0, 5, 1, 2);
    OBMC_FILTER (x+7, 0, 0, 4, 2, 2);
}

/* obmc for 1 8x8 luma block */
static inline void obmc_motion(MpegEncContext *s,
                               uint8_t *dest, uint8_t *src,
                               int src_x, int src_y,
                               op_pixels_func *pix_op,
                               int16_t mv[5][2]/* mid top left right bottom*/)
#define MID    0
{
    int i;
    uint8_t *ptr[5];

//    av_assert2(s->quarter_sample==0);

    for(i=0; i<5; i++){
        if(i && mv[i][0]==mv[MID][0] && mv[i][1]==mv[MID][1]){
            ptr[i]= ptr[MID];
        }else{
            ptr[i]= s->obmc_scratchpad + 8*(i&1) + s->linesize*8*(i>>1);
            hpel_motion(s, ptr[i], src,
                        src_x, src_y,
                        pix_op,
                        mv[i][0], mv[i][1]);
        }
    }

    put_obmc(dest, ptr, s->linesize);
}

static inline void prefetch_motion(MpegEncContext *s, uint8_t **pix, int dir){
    /* fetch pixels for estimated mv 4 macroblocks ahead
     * optimized for 64byte cache lines */
	LOGD("prefetch_motion Called\n");
    const int shift = s->quarter_sample ? 2 : 1;
    const int mx= (s->mv[dir][0][0]>>shift) + 16*s->mb_x + 8;
    const int my= (s->mv[dir][0][1]>>shift) + 16*s->mb_y;
    int off= mx + (my + (s->mb_x&3)*4)*s->linesize + 64;
    s->vdsp.prefetch(pix[0]+off, s->linesize, 4);
    off= (mx>>1) + ((my>>1) + (s->mb_x&7))*s->uvlinesize + 64;
    s->vdsp.prefetch(pix[1]+off, pix[2]-pix[1], 2);
}


/**
 * h263 chroma 4mv motion compensation.
 */
static void chroma_4mv_motion(MpegEncContext *s,
                              uint8_t *dest_cb, uint8_t *dest_cr,
                              uint8_t **ref_picture,
                              op_pixels_func *pix_op,
                              int mx, int my)
{
    int dxy, emu=0, src_x, src_y, offset;
    uint8_t *ptr;

    /* In case of 8X8, we construct a single chroma motion vector
       with a special rounding */
    mx= ff_h263_round_chroma(mx);
    my= ff_h263_round_chroma(my);

    dxy = ((my & 1) << 1) | (mx & 1);
    mx >>= 1;
    my >>= 1;

    src_x = s->mb_x * 8 + mx;
    src_y = s->mb_y * 8 + my;
    src_x = av_clip_c(src_x, -8, (s->width >> 1));
    if (src_x == (s->width >> 1))
        dxy &= ~1;
    src_y = av_clip_c(src_y, -8, (s->height >> 1));
    if (src_y == (s->height >> 1))
        dxy &= ~2;

    offset = src_y * s->uvlinesize + src_x;
    ptr = ref_picture[1] + offset;
    if(s->flags&CODEC_FLAG_EMU_EDGE){
        if(   (unsigned)src_x > FFMAX((s->h_edge_pos>>1) - (dxy &1) - 8, 0)
           || (unsigned)src_y > FFMAX((s->v_edge_pos>>1) - (dxy>>1) - 8, 0)){
            s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, s->uvlinesize,
                                9, 9, src_x, src_y,
                                s->h_edge_pos>>1, s->v_edge_pos>>1);
            ptr= s->edge_emu_buffer;
            emu=1;
        }
    }
    pix_op[dxy](dest_cb, ptr, s->uvlinesize, 8);

    ptr = ref_picture[2] + offset;
    if(emu){
        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, s->uvlinesize,
                            9, 9, src_x, src_y,
                            s->h_edge_pos>>1, s->v_edge_pos>>1);
        ptr= s->edge_emu_buffer;
    }
    pix_op[dxy](dest_cr, ptr, s->uvlinesize, 8);
}



static void gmc1_motion(MpegEncContext *s,
                        uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
                        uint8_t **ref_picture)
{
    uint8_t *ptr;
    int offset, src_x, src_y, linesize, uvlinesize;
    int motion_x, motion_y;
    int emu=0;

    motion_x= s->sprite_offset[0][0];
    motion_y= s->sprite_offset[0][1];
    src_x = s->mb_x * 16 + (motion_x >> (s->sprite_warping_accuracy+1));
    src_y = s->mb_y * 16 + (motion_y >> (s->sprite_warping_accuracy+1));
    motion_x<<=(3-s->sprite_warping_accuracy);
    motion_y<<=(3-s->sprite_warping_accuracy);
    src_x = av_clip_c(src_x, -16, s->width);
    if (src_x == s->width)
        motion_x =0;
    src_y = av_clip_c(src_y, -16, s->height);
    if (src_y == s->height)
        motion_y =0;

    linesize = s->linesize;
    uvlinesize = s->uvlinesize;

    ptr = ref_picture[0] + (src_y * linesize) + src_x;

    if(s->flags&CODEC_FLAG_EMU_EDGE){
        if(   (unsigned)src_x >= FFMAX(s->h_edge_pos - 17, 0)
           || (unsigned)src_y >= FFMAX(s->v_edge_pos - 17, 0)){
            s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, linesize, 17, 17, src_x, src_y, s->h_edge_pos, s->v_edge_pos);
            ptr= s->edge_emu_buffer;
        }
    }

    if((motion_x|motion_y)&7){
        s->dsp.gmc1(dest_y  , ptr  , linesize, 16, motion_x&15, motion_y&15, 128 - s->no_rounding);
        s->dsp.gmc1(dest_y+8, ptr+8, linesize, 16, motion_x&15, motion_y&15, 128 - s->no_rounding);
    }else{
        int dxy;

        dxy= ((motion_x>>3)&1) | ((motion_y>>2)&2);
        if (s->no_rounding){
            s->dsp.put_no_rnd_pixels_tab[0][dxy](dest_y, ptr, linesize, 16);
        }else{
            s->dsp.put_pixels_tab       [0][dxy](dest_y, ptr, linesize, 16);
        }
    }

    if(CONFIG_GRAY && (s->flags&CODEC_FLAG_GRAY)) return;

    motion_x= s->sprite_offset[1][0];
    motion_y= s->sprite_offset[1][1];
    src_x = s->mb_x * 8 + (motion_x >> (s->sprite_warping_accuracy+1));
    src_y = s->mb_y * 8 + (motion_y >> (s->sprite_warping_accuracy+1));
    motion_x<<=(3-s->sprite_warping_accuracy);
    motion_y<<=(3-s->sprite_warping_accuracy);
    src_x = av_clip_c(src_x, -8, s->width>>1);
    if (src_x == s->width>>1)
        motion_x =0;
    src_y = av_clip_c(src_y, -8, s->height>>1);
    if (src_y == s->height>>1)
        motion_y =0;

    offset = (src_y * uvlinesize) + src_x;
    ptr = ref_picture[1] + offset;
    if(s->flags&CODEC_FLAG_EMU_EDGE){
        if(   (unsigned)src_x >= FFMAX((s->h_edge_pos>>1) - 9, 0)
           || (unsigned)src_y >= FFMAX((s->v_edge_pos>>1) - 9, 0)){
            s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, uvlinesize, 9, 9, src_x, src_y, s->h_edge_pos>>1, s->v_edge_pos>>1);
            ptr= s->edge_emu_buffer;
            emu=1;
        }
    }
    s->dsp.gmc1(dest_cb, ptr, uvlinesize, 8, motion_x&15, motion_y&15, 128 - s->no_rounding);

    ptr = ref_picture[2] + offset;
    if(emu){
        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, uvlinesize, 9, 9, src_x, src_y, s->h_edge_pos>>1, s->v_edge_pos>>1);
        ptr= s->edge_emu_buffer;
    }
    s->dsp.gmc1(dest_cr, ptr, uvlinesize, 8, motion_x&15, motion_y&15, 128 - s->no_rounding);

    return;
}

static inline void qpel_motion(MpegEncContext *s,
                               uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
                               int field_based, int bottom_field, int field_select,
                               uint8_t **ref_picture, op_pixels_func (*pix_op)[4],
                               qpel_mc_func (*qpix_op)[16],
                               int motion_x, int motion_y, int h)
{
    uint8_t *ptr_y, *ptr_cb, *ptr_cr;
    int dxy, uvdxy, mx, my, src_x, src_y, uvsrc_x, uvsrc_y, v_edge_pos, linesize, uvlinesize;

    dxy = ((motion_y & 3) << 2) | (motion_x & 3);
    src_x = s->mb_x *  16                 + (motion_x >> 2);
    src_y = s->mb_y * (16 >> field_based) + (motion_y >> 2);

    v_edge_pos = s->v_edge_pos >> field_based;
    linesize = s->linesize << field_based;
    uvlinesize = s->uvlinesize << field_based;

    if(field_based){
        mx= motion_x/2;
        my= motion_y>>1;
    }else if(s->workaround_bugs&FF_BUG_QPEL_CHROMA2){
        static const int rtab[8]= {0,0,1,1,0,0,0,1};
        mx= (motion_x>>1) + rtab[motion_x&7];
        my= (motion_y>>1) + rtab[motion_y&7];
    }else if(s->workaround_bugs&FF_BUG_QPEL_CHROMA){
        mx= (motion_x>>1)|(motion_x&1);
        my= (motion_y>>1)|(motion_y&1);
    }else{
        mx= motion_x/2;
        my= motion_y/2;
    }
    mx= (mx>>1)|(mx&1);
    my= (my>>1)|(my&1);

    uvdxy= (mx&1) | ((my&1)<<1);
    mx>>=1;
    my>>=1;

    uvsrc_x = s->mb_x *  8                 + mx;
    uvsrc_y = s->mb_y * (8 >> field_based) + my;

    ptr_y  = ref_picture[0] +   src_y *   linesize +   src_x;
    ptr_cb = ref_picture[1] + uvsrc_y * uvlinesize + uvsrc_x;
    ptr_cr = ref_picture[2] + uvsrc_y * uvlinesize + uvsrc_x;

    if(   (unsigned)src_x > FFMAX(s->h_edge_pos - (motion_x&3) - 16, 0)
       || (unsigned)src_y > FFMAX(   v_edge_pos - (motion_y&3) - h , 0)){
        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr_y, s->linesize,
                            17, 17+field_based, src_x, src_y<<field_based,
                            s->h_edge_pos, s->v_edge_pos);
        ptr_y= s->edge_emu_buffer;
        if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY)){
            uint8_t *uvbuf= s->edge_emu_buffer + 18*s->linesize;
            s->vdsp.emulated_edge_mc(uvbuf, ptr_cb, s->uvlinesize,
                                9, 9 + field_based,
                                uvsrc_x, uvsrc_y<<field_based,
                                s->h_edge_pos>>1, s->v_edge_pos>>1);
            s->vdsp.emulated_edge_mc(uvbuf + 16, ptr_cr, s->uvlinesize,
                                9, 9 + field_based,
                                uvsrc_x, uvsrc_y<<field_based,
                                s->h_edge_pos>>1, s->v_edge_pos>>1);
            ptr_cb= uvbuf;
            ptr_cr= uvbuf + 16;
        }
    }

    if(!field_based)
        qpix_op[0][dxy](dest_y, ptr_y, linesize);
    else{
        if(bottom_field){
            dest_y += s->linesize;
            dest_cb+= s->uvlinesize;
            dest_cr+= s->uvlinesize;
        }

        if(field_select){
            ptr_y  += s->linesize;
            ptr_cb += s->uvlinesize;
            ptr_cr += s->uvlinesize;
        }
        //damn interlaced mode
        //FIXME boundary mirroring is not exactly correct here
        qpix_op[1][dxy](dest_y  , ptr_y  , linesize);
        qpix_op[1][dxy](dest_y+8, ptr_y+8, linesize);
    }
    if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY)){
        pix_op[1][uvdxy](dest_cr, ptr_cr, uvlinesize, h >> 1);
        pix_op[1][uvdxy](dest_cb, ptr_cb, uvlinesize, h >> 1);
    }
}


static inline
void mpeg_motion_internal(MpegEncContext *s,
                 uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
                 int field_based, int bottom_field, int field_select,
                 uint8_t **ref_picture, op_pixels_func (*pix_op)[4],
                 int motion_x, int motion_y, int h, int is_mpeg12, int mb_y)
{
    uint8_t *ptr_y, *ptr_cb, *ptr_cr;
    int dxy, uvdxy, mx, my, src_x, src_y,
        uvsrc_x, uvsrc_y, v_edge_pos, uvlinesize, linesize;

#if 0
if(s->quarter_sample)
{
    motion_x>>=1;
    motion_y>>=1;
}
#endif

    v_edge_pos = s->v_edge_pos >> field_based;
    linesize   = s->current_picture.f.linesize[0] << field_based;
    uvlinesize = s->current_picture.f.linesize[1] << field_based;

    dxy = ((motion_y & 1) << 1) | (motion_x & 1);
    src_x = s->mb_x* 16               + (motion_x >> 1);
    src_y =(   mb_y<<(4-field_based)) + (motion_y >> 1);

    if (!is_mpeg12 && s->out_format == FMT_H263) {
        if((s->workaround_bugs & FF_BUG_HPEL_CHROMA) && field_based){
            mx = (motion_x>>1)|(motion_x&1);
            my = motion_y >>1;
            uvdxy = ((my & 1) << 1) | (mx & 1);
            uvsrc_x = s->mb_x* 8               + (mx >> 1);
            uvsrc_y =(   mb_y<<(3-field_based))+ (my >> 1);
        }else{
            uvdxy = dxy | (motion_y & 2) | ((motion_x & 2) >> 1);
            uvsrc_x = src_x>>1;
            uvsrc_y = src_y>>1;
        }
    }else if(!is_mpeg12 && s->out_format == FMT_H261){//even chroma mv's are full pel in H261
        mx = motion_x / 4;
        my = motion_y / 4;
        uvdxy = 0;
        uvsrc_x = s->mb_x*8 + mx;
        uvsrc_y =    mb_y*8 + my;
    } else {
        if(s->chroma_y_shift){
            mx = motion_x / 2;
            my = motion_y / 2;
            uvdxy = ((my & 1) << 1) | (mx & 1);
            uvsrc_x = s->mb_x* 8               + (mx >> 1);
            uvsrc_y =(   mb_y<<(3-field_based))+ (my >> 1);
        } else {
            if(s->chroma_x_shift){
            //Chroma422
                mx = motion_x / 2;
                uvdxy = ((motion_y & 1) << 1) | (mx & 1);
                uvsrc_x = s->mb_x* 8           + (mx >> 1);
                uvsrc_y = src_y;
            } else {
            //Chroma444
                uvdxy = dxy;
                uvsrc_x = src_x;
                uvsrc_y = src_y;
            }
        }
    }

    ptr_y  = ref_picture[0] + src_y * linesize + src_x;
    ptr_cb = ref_picture[1] + uvsrc_y * uvlinesize + uvsrc_x;
    ptr_cr = ref_picture[2] + uvsrc_y * uvlinesize + uvsrc_x;

    if(   (unsigned)src_x > FFMAX(s->h_edge_pos - (motion_x&1) - 16, 0)
       || (unsigned)src_y > FFMAX(   v_edge_pos - (motion_y&1) - h , 0)){
            if(is_mpeg12 || s->codec_id == AV_CODEC_ID_MPEG2VIDEO ||
               s->codec_id == AV_CODEC_ID_MPEG1VIDEO){
                printf("MPEG motion vector out of boundary (%d %d)\n", src_x, src_y);
                return;
            }
            s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr_y, s->linesize,
                                17, 17+field_based,
                                src_x, src_y<<field_based,
                                s->h_edge_pos, s->v_edge_pos);
            ptr_y = s->edge_emu_buffer;
            if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY)){
                uint8_t *uvbuf= s->edge_emu_buffer+18*s->linesize;
                s->vdsp.emulated_edge_mc(uvbuf ,
                                    ptr_cb, s->uvlinesize,
                                    9, 9+field_based,
                                    uvsrc_x, uvsrc_y<<field_based,
                                    s->h_edge_pos>>1, s->v_edge_pos>>1);
                s->vdsp.emulated_edge_mc(uvbuf+16,
                                    ptr_cr, s->uvlinesize,
                                    9, 9+field_based,
                                    uvsrc_x, uvsrc_y<<field_based,
                                    s->h_edge_pos>>1, s->v_edge_pos>>1);
                ptr_cb= uvbuf;
                ptr_cr= uvbuf+16;
            }
    }

    if(bottom_field){ //FIXME use this for field pix too instead of the obnoxious hack which changes picture.data
        dest_y += s->linesize;
        dest_cb+= s->uvlinesize;
        dest_cr+= s->uvlinesize;
    }

    if(field_select){
        ptr_y += s->linesize;
        ptr_cb+= s->uvlinesize;
        ptr_cr+= s->uvlinesize;
    }

    pix_op[0][dxy](dest_y, ptr_y, linesize, h);

    if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY)){
        pix_op[s->chroma_x_shift][uvdxy]
                (dest_cb, ptr_cb, uvlinesize, h >> s->chroma_y_shift);
        pix_op[s->chroma_x_shift][uvdxy]
                (dest_cr, ptr_cr, uvlinesize, h >> s->chroma_y_shift);
    }
    if(!is_mpeg12 && (CONFIG_H261_ENCODER || CONFIG_H261_DECODER) &&
        //たぶんここは通らないので省略
    		s->out_format == FMT_H261){
//        ff_h261_loop_filter(s);
    }
}

/* apply one mpeg motion vector to the three components */
static void mpeg_motion(MpegEncContext *s,
                        uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
                        int field_select, uint8_t **ref_picture,
                        op_pixels_func (*pix_op)[4],
                        int motion_x, int motion_y, int h, int mb_y)
{
#if !CONFIG_SMALL
    if(s->out_format == FMT_MPEG1)
        mpeg_motion_internal(s, dest_y, dest_cb, dest_cr, 0, 0,
                    field_select, ref_picture, pix_op,
                    motion_x, motion_y, h, 1, mb_y);
    else
#endif
        mpeg_motion_internal(s, dest_y, dest_cb, dest_cr, 0, 0,
                    field_select, ref_picture, pix_op,
                    motion_x, motion_y, h, 0, mb_y);
}

static void gmc_motion(MpegEncContext *s,
                       uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
                       uint8_t **ref_picture)
{
    uint8_t *ptr;
    int linesize, uvlinesize;
    const int a= s->sprite_warping_accuracy;
    int ox, oy;

    linesize = s->linesize;
    uvlinesize = s->uvlinesize;

    ptr = ref_picture[0];

    ox= s->sprite_offset[0][0] + s->sprite_delta[0][0]*s->mb_x*16 + s->sprite_delta[0][1]*s->mb_y*16;
    oy= s->sprite_offset[0][1] + s->sprite_delta[1][0]*s->mb_x*16 + s->sprite_delta[1][1]*s->mb_y*16;

    s->dsp.gmc(dest_y, ptr, linesize, 16,
           ox,
           oy,
           s->sprite_delta[0][0], s->sprite_delta[0][1],
           s->sprite_delta[1][0], s->sprite_delta[1][1],
           a+1, (1<<(2*a+1)) - s->no_rounding,
           s->h_edge_pos, s->v_edge_pos);
    s->dsp.gmc(dest_y+8, ptr, linesize, 16,
           ox + s->sprite_delta[0][0]*8,
           oy + s->sprite_delta[1][0]*8,
           s->sprite_delta[0][0], s->sprite_delta[0][1],
           s->sprite_delta[1][0], s->sprite_delta[1][1],
           a+1, (1<<(2*a+1)) - s->no_rounding,
           s->h_edge_pos, s->v_edge_pos);

    if(CONFIG_GRAY && (s->flags&CODEC_FLAG_GRAY)) return;

    ox= s->sprite_offset[1][0] + s->sprite_delta[0][0]*s->mb_x*8 + s->sprite_delta[0][1]*s->mb_y*8;
    oy= s->sprite_offset[1][1] + s->sprite_delta[1][0]*s->mb_x*8 + s->sprite_delta[1][1]*s->mb_y*8;

    ptr = ref_picture[1];
    s->dsp.gmc(dest_cb, ptr, uvlinesize, 8,
           ox,
           oy,
           s->sprite_delta[0][0], s->sprite_delta[0][1],
           s->sprite_delta[1][0], s->sprite_delta[1][1],
           a+1, (1<<(2*a+1)) - s->no_rounding,
           s->h_edge_pos>>1, s->v_edge_pos>>1);

    ptr = ref_picture[2];
    s->dsp.gmc(dest_cr, ptr, uvlinesize, 8,
           ox,
           oy,
           s->sprite_delta[0][0], s->sprite_delta[0][1],
           s->sprite_delta[1][0], s->sprite_delta[1][1],
           a+1, (1<<(2*a+1)) - s->no_rounding,
           s->h_edge_pos>>1, s->v_edge_pos>>1);
}



void ff_mspel_motion(MpegEncContext *s,
                               uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
                               uint8_t **ref_picture, op_pixels_func (*pix_op)[4],
                               int motion_x, int motion_y, int h)
{
    Wmv2Context * const w= (Wmv2Context*)s;
    uint8_t *ptr;
    int dxy, offset, mx, my, src_x, src_y, v_edge_pos, linesize, uvlinesize;
    int emu=0;

    dxy = ((motion_y & 1) << 1) | (motion_x & 1);
    dxy = 2*dxy + w->hshift;
    src_x = s->mb_x * 16 + (motion_x >> 1);
    src_y = s->mb_y * 16 + (motion_y >> 1);

    /* WARNING: do no forget half pels */
    v_edge_pos = s->v_edge_pos;
    src_x = av_clip_c(src_x, -16, s->width);
    src_y = av_clip_c(src_y, -16, s->height);

    if(src_x<=-16 || src_x >= s->width)
        dxy &= ~3;
    if(src_y<=-16 || src_y >= s->height)
        dxy &= ~4;

    linesize   = s->linesize;
    uvlinesize = s->uvlinesize;
    ptr = ref_picture[0] + (src_y * linesize) + src_x;

    if(s->flags&CODEC_FLAG_EMU_EDGE){
        if(src_x<1 || src_y<1 || src_x + 17  >= s->h_edge_pos
                              || src_y + h+1 >= v_edge_pos){
            s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr - 1 - s->linesize, s->linesize, 19, 19,
                             src_x-1, src_y-1, s->h_edge_pos, s->v_edge_pos);
            ptr= s->edge_emu_buffer + 1 + s->linesize;
            emu=1;
        }
    }

    s->dsp.put_mspel_pixels_tab[dxy](dest_y             , ptr             , linesize);
    s->dsp.put_mspel_pixels_tab[dxy](dest_y+8           , ptr+8           , linesize);
    s->dsp.put_mspel_pixels_tab[dxy](dest_y  +8*linesize, ptr  +8*linesize, linesize);
    s->dsp.put_mspel_pixels_tab[dxy](dest_y+8+8*linesize, ptr+8+8*linesize, linesize);

    if(s->flags&CODEC_FLAG_GRAY) return;

    if (s->out_format == FMT_H263) {
        dxy = 0;
        if ((motion_x & 3) != 0)
            dxy |= 1;
        if ((motion_y & 3) != 0)
            dxy |= 2;
        mx = motion_x >> 2;
        my = motion_y >> 2;
    } else {
        mx = motion_x / 2;
        my = motion_y / 2;
        dxy = ((my & 1) << 1) | (mx & 1);
        mx >>= 1;
        my >>= 1;
    }

    src_x = s->mb_x * 8 + mx;
    src_y = s->mb_y * 8 + my;
    src_x = av_clip_c(src_x, -8, s->width >> 1);
    if (src_x == (s->width >> 1))
        dxy &= ~1;
    src_y = av_clip_c(src_y, -8, s->height >> 1);
    if (src_y == (s->height >> 1))
        dxy &= ~2;
    offset = (src_y * uvlinesize) + src_x;
    ptr = ref_picture[1] + offset;
    if(emu){
        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, s->uvlinesize, 9, 9,
                         src_x, src_y, s->h_edge_pos>>1, s->v_edge_pos>>1);
        ptr= s->edge_emu_buffer;
    }
    pix_op[1][dxy](dest_cb, ptr, uvlinesize, h >> 1);

    ptr = ref_picture[2] + offset;
    if(emu){
        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, s->uvlinesize, 9, 9,
                         src_x, src_y, s->h_edge_pos>>1, s->v_edge_pos>>1);
        ptr= s->edge_emu_buffer;
    }
    pix_op[1][dxy](dest_cr, ptr, uvlinesize, h >> 1);
}



static void mpeg_motion_field(MpegEncContext *s, uint8_t *dest_y,
                              uint8_t *dest_cb, uint8_t *dest_cr,
                              int bottom_field, int field_select,
                              uint8_t **ref_picture,
                              op_pixels_func (*pix_op)[4],
                              int motion_x, int motion_y, int h, int mb_y)
{
#if !CONFIG_SMALL
    if(s->out_format == FMT_MPEG1)
        mpeg_motion_internal(s, dest_y, dest_cb, dest_cr, 1,
                    bottom_field, field_select, ref_picture, pix_op,
                    motion_x, motion_y, h, 1, mb_y);
    else
#endif
        mpeg_motion_internal(s, dest_y, dest_cb, dest_cr, 1,
                    bottom_field, field_select, ref_picture, pix_op,
                    motion_x, motion_y, h, 0, mb_y);
}

/**
 * motion compensation of a single macroblock
 * @param s context
 * @param dest_y luma destination pointer
 * @param dest_cb chroma cb/u destination pointer
 * @param dest_cr chroma cr/v destination pointer
 * @param dir direction (0->forward, 1->backward)
 * @param ref_picture array[3] of pointers to the 3 planes of the reference picture
 * @param pix_op halfpel motion compensation function (average or put normally)
 * @param qpix_op qpel motion compensation function (average or put normally)
 * the motion vectors are taken from s->mv and the MV type from s->mv_type
 */
static inline void MPV_motion_internal(MpegEncContext *s,
                              uint8_t *dest_y, uint8_t *dest_cb,
                              uint8_t *dest_cr, int dir,
                              uint8_t **ref_picture,
                              op_pixels_func (*pix_op)[4],
                              qpel_mc_func (*qpix_op)[16], int is_mpeg12)
{
	LOGD("MPV_motion_internal Called\n");
    int dxy, mx, my, src_x, src_y, motion_x, motion_y;
    int mb_x, mb_y, i;
    uint8_t *ptr, *dest;

    mb_x = s->mb_x;
    mb_y = s->mb_y;

    prefetch_motion(s, ref_picture, dir);

    if(!is_mpeg12 && s->obmc && s->pict_type != AV_PICTURE_TYPE_B){
        LOCAL_ALIGNED_8(int16_t, mv_cache, [4], [4][2]);
        AVFrame *cur_frame = &s->current_picture.f;
        const int xy= s->mb_x + s->mb_y*s->mb_stride;
        const int mot_stride= s->b8_stride;
        const int mot_xy= mb_x*2 + mb_y*2*mot_stride;

//        av_assert2(!s->mb_skipped);


        //何故かここが通らない my_aliasがよくわからない
        AV_COPY32(mv_cache[1][1], cur_frame->motion_val[0][mot_xy    ]);
        AV_COPY32(mv_cache[1][2], cur_frame->motion_val[0][mot_xy + 1]);

        AV_COPY32(mv_cache[2][1], cur_frame->motion_val[0][mot_xy + mot_stride    ]);
        AV_COPY32(mv_cache[2][2], cur_frame->motion_val[0][mot_xy + mot_stride + 1]);

        AV_COPY32(mv_cache[3][1], cur_frame->motion_val[0][mot_xy + mot_stride    ]);
        AV_COPY32(mv_cache[3][2], cur_frame->motion_val[0][mot_xy + mot_stride + 1]);

        if (mb_y == 0 || IS_INTRA(cur_frame->mb_type[xy - s->mb_stride])) {
            AV_COPY32(mv_cache[0][1], mv_cache[1][1]);
            AV_COPY32(mv_cache[0][2], mv_cache[1][2]);
        }else{
            AV_COPY32(mv_cache[0][1], cur_frame->motion_val[0][mot_xy - mot_stride    ]);
            AV_COPY32(mv_cache[0][2], cur_frame->motion_val[0][mot_xy - mot_stride + 1]);
        }

        if (mb_x == 0 || IS_INTRA(cur_frame->mb_type[xy - 1])) {
            AV_COPY32(mv_cache[1][0], mv_cache[1][1]);
            AV_COPY32(mv_cache[2][0], mv_cache[2][1]);
        }else{
            AV_COPY32(mv_cache[1][0], cur_frame->motion_val[0][mot_xy - 1]);
            AV_COPY32(mv_cache[2][0], cur_frame->motion_val[0][mot_xy - 1 + mot_stride]);
        }

        if (mb_x + 1 >= s->mb_width || IS_INTRA(cur_frame->mb_type[xy + 1])) {
            AV_COPY32(mv_cache[1][3], mv_cache[1][2]);
            AV_COPY32(mv_cache[2][3], mv_cache[2][2]);
        }else{
            AV_COPY32(mv_cache[1][3], cur_frame->motion_val[0][mot_xy + 2]);
            AV_COPY32(mv_cache[2][3], cur_frame->motion_val[0][mot_xy + 2 + mot_stride]);
        }

        mx = 0;
        my = 0;
        for(i=0;i<4;i++) {
            const int x= (i&1)+1;
            const int y= (i>>1)+1;
            int16_t mv[5][2]= {
                {mv_cache[y][x  ][0], mv_cache[y][x  ][1]},
                {mv_cache[y-1][x][0], mv_cache[y-1][x][1]},
                {mv_cache[y][x-1][0], mv_cache[y][x-1][1]},
                {mv_cache[y][x+1][0], mv_cache[y][x+1][1]},
                {mv_cache[y+1][x][0], mv_cache[y+1][x][1]}};
            //FIXME cleanup
            obmc_motion(s, dest_y + ((i & 1) * 8) + (i >> 1) * 8 * s->linesize,
                        ref_picture[0],
                        mb_x * 16 + (i & 1) * 8, mb_y * 16 + (i >>1) * 8,
                        pix_op[1],
                        mv);

            mx += mv[0][0];
            my += mv[0][1];
        }
        if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY))
            chroma_4mv_motion(s, dest_cb, dest_cr, ref_picture, pix_op[1], mx, my);

        return;
    }

    switch(s->mv_type) {
    case MV_TYPE_16X16:
        if(s->mcsel){
            if(s->real_sprite_warping_points==1){
                gmc1_motion(s, dest_y, dest_cb, dest_cr,
                            ref_picture);
            }else{
                gmc_motion(s, dest_y, dest_cb, dest_cr,
                            ref_picture);
            }
        }else if(!is_mpeg12 && s->quarter_sample){
            qpel_motion(s, dest_y, dest_cb, dest_cr,
                        0, 0, 0,
                        ref_picture, pix_op, qpix_op,
                        s->mv[dir][0][0], s->mv[dir][0][1], 16);
        } else if (!is_mpeg12 && (CONFIG_WMV2_DECODER || CONFIG_WMV2_ENCODER) &&
                   s->mspel && s->codec_id == AV_CODEC_ID_WMV2) {
            ff_mspel_motion(s, dest_y, dest_cb, dest_cr,
                        ref_picture, pix_op,
                        s->mv[dir][0][0], s->mv[dir][0][1], 16);
        }else
        {
            mpeg_motion(s, dest_y, dest_cb, dest_cr, 0,
                        ref_picture, pix_op,
                        s->mv[dir][0][0], s->mv[dir][0][1], 16, mb_y);
        }
        break;
    case MV_TYPE_8X8:
    if (!is_mpeg12) {
        mx = 0;
        my = 0;
        if(s->quarter_sample){
            for(i=0;i<4;i++) {
                motion_x = s->mv[dir][i][0];
                motion_y = s->mv[dir][i][1];

                dxy = ((motion_y & 3) << 2) | (motion_x & 3);
                src_x = mb_x * 16 + (motion_x >> 2) + (i & 1) * 8;
                src_y = mb_y * 16 + (motion_y >> 2) + (i >>1) * 8;

                /* WARNING: do no forget half pels */
                src_x = av_clip_c(src_x, -16, s->width);
                if (src_x == s->width)
                    dxy &= ~3;
                src_y = av_clip_c(src_y, -16, s->height);
                if (src_y == s->height)
                    dxy &= ~12;

                ptr = ref_picture[0] + (src_y * s->linesize) + (src_x);
                if(s->flags&CODEC_FLAG_EMU_EDGE){
                    if(   (unsigned)src_x > FFMAX(s->h_edge_pos - (motion_x&3) - 8, 0)
                       || (unsigned)src_y > FFMAX(s->v_edge_pos - (motion_y&3) - 8, 0)){
                        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr,
                                            s->linesize, 9, 9,
                                            src_x, src_y,
                                            s->h_edge_pos, s->v_edge_pos);
                        ptr= s->edge_emu_buffer;
                    }
                }
                dest = dest_y + ((i & 1) * 8) + (i >> 1) * 8 * s->linesize;
                qpix_op[1][dxy](dest, ptr, s->linesize);

                mx += s->mv[dir][i][0]/2;
                my += s->mv[dir][i][1]/2;
            }
        }else{
            for(i=0;i<4;i++) {
                hpel_motion(s, dest_y + ((i & 1) * 8) + (i >> 1) * 8 * s->linesize,
                            ref_picture[0],
                            mb_x * 16 + (i & 1) * 8, mb_y * 16 + (i >>1) * 8,
                            pix_op[1],
                            s->mv[dir][i][0], s->mv[dir][i][1]);

                mx += s->mv[dir][i][0];
                my += s->mv[dir][i][1];
            }
        }

        if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY))
            chroma_4mv_motion(s, dest_cb, dest_cr, ref_picture, pix_op[1], mx, my);
    }
        break;
    case MV_TYPE_FIELD:
        if (s->picture_structure == PICT_FRAME) {
            if(!is_mpeg12 && s->quarter_sample){
                for(i=0; i<2; i++){
                    qpel_motion(s, dest_y, dest_cb, dest_cr,
                                1, i, s->field_select[dir][i],
                                ref_picture, pix_op, qpix_op,
                                s->mv[dir][i][0], s->mv[dir][i][1], 8);
                }
            }else{
                /* top field */
                mpeg_motion_field(s, dest_y, dest_cb, dest_cr,
                                  0, s->field_select[dir][0],
                                  ref_picture, pix_op,
                                  s->mv[dir][0][0], s->mv[dir][0][1], 8, mb_y);
                /* bottom field */
                mpeg_motion_field(s, dest_y, dest_cb, dest_cr,
                                  1, s->field_select[dir][1],
                                  ref_picture, pix_op,
                                  s->mv[dir][1][0], s->mv[dir][1][1], 8, mb_y);
            }
        } else {
            if(s->picture_structure != s->field_select[dir][0] + 1 && s->pict_type != AV_PICTURE_TYPE_B && !s->first_field){
                ref_picture = s->current_picture_ptr->f.data;
            }

            mpeg_motion(s, dest_y, dest_cb, dest_cr,
                        s->field_select[dir][0],
                        ref_picture, pix_op,
                        s->mv[dir][0][0], s->mv[dir][0][1], 16, mb_y>>1);
        }
        break;
    case MV_TYPE_16X8:
        for(i=0; i<2; i++){
            uint8_t ** ref2picture;

            if(s->picture_structure == s->field_select[dir][i] + 1
               || s->pict_type == AV_PICTURE_TYPE_B || s->first_field){
                ref2picture= ref_picture;
            }else{
                ref2picture = s->current_picture_ptr->f.data;
            }

            mpeg_motion(s, dest_y, dest_cb, dest_cr,
                        s->field_select[dir][i],
                        ref2picture, pix_op,
                        s->mv[dir][i][0], s->mv[dir][i][1] + 16*i, 8, mb_y>>1);

            dest_y += 16*s->linesize;
            dest_cb+= (16>>s->chroma_y_shift)*s->uvlinesize;
            dest_cr+= (16>>s->chroma_y_shift)*s->uvlinesize;
        }
        break;
    case MV_TYPE_DMV:
        if(s->picture_structure == PICT_FRAME){
            for(i=0; i<2; i++){
                int j;
                for(j=0; j<2; j++){
                    mpeg_motion_field(s, dest_y, dest_cb, dest_cr,
                                      j, j^i, ref_picture, pix_op,
                                      s->mv[dir][2*i + j][0],
                                      s->mv[dir][2*i + j][1], 8, mb_y);
                }
                pix_op = s->dsp.avg_pixels_tab;
            }
        }else{
            for(i=0; i<2; i++){
                mpeg_motion(s, dest_y, dest_cb, dest_cr,
                            s->picture_structure != i+1,
                            ref_picture, pix_op,
                            s->mv[dir][2*i][0],s->mv[dir][2*i][1],16, mb_y>>1);

                // after put we make avg of the same block
                pix_op=s->dsp.avg_pixels_tab;

                //opposite parity is always in the same frame if this is second field
                if(!s->first_field){
                    ref_picture = s->current_picture_ptr->f.data;
                }
            }
        }
    break;
//    default: av_assert2(0);
    }
//	LOGD("MPV_motion_internal END\n");
}

void ff_MPV_motion(MpegEncContext *s,
                   uint8_t *dest_y, uint8_t *dest_cb,
                   uint8_t *dest_cr, int dir,
                   uint8_t **ref_picture,
                   op_pixels_func (*pix_op)[4],
                   qpel_mc_func (*qpix_op)[16])
{
//	LOGD("ff_MPV_motion Called\n");
#if !CONFIG_SMALL
    if(s->out_format == FMT_MPEG1)
        MPV_motion_internal(s, dest_y, dest_cb, dest_cr, dir,
                            ref_picture, pix_op, qpix_op, 1);
    else
#endif
        MPV_motion_internal(s, dest_y, dest_cb, dest_cr, dir,
                            ref_picture, pix_op, qpix_op, 0);
}

static void get_visual_weight(int16_t *weight, uint8_t *ptr, int stride)
{
    int x, y;
    // FIXME optimize
    for (y = 0; y < 8; y++) {
        for (x = 0; x < 8; x++) {
            int x2, y2;
            int sum = 0;
            int sqr = 0;
            int count = 0;

            for (y2 = FFMAX(y - 1, 0); y2 < FFMIN(8, y + 2); y2++) {
                for (x2= FFMAX(x - 1, 0); x2 < FFMIN(8, x + 2); x2++) {
                    int v = ptr[x2 + y2 * stride];
                    sum += v;
                    sqr += v * v;
                    count++;
                }
            }
            weight[x + 8 * y]= (36 * ff_sqrt(count * sqr - sum * sum)) / count;
        }
    }
}

static inline int get_b_cbp(MpegEncContext * s, int16_t block[6][64],
                            int motion_x, int motion_y, int mb_type)
{
    int cbp = 0, i;

    if (s->mpv_flags & FF_MPV_FLAG_CBP_RD) {
        int score = 0;
        const int lambda = s->lambda2 >> (FF_LAMBDA_SHIFT - 6);

        for (i = 0; i < 6; i++)
            if (s->coded_score[i] < 0) {
                score += s->coded_score[i];
                cbp   |= 1 << (5 - i);
            }

        if (cbp) {
            int zero_score = -6;
            if ((motion_x | motion_y | s->dquant | mb_type) == 0)
                zero_score -= 4; //2*MV + mb_type + cbp bit

            zero_score *= lambda;
            if (zero_score <= score)
                cbp = 0;
        }

        for (i = 0; i < 6; i++) {
            if (s->block_last_index[i] >= 0 && ((cbp >> (5 - i)) & 1) == 0) {
                s->block_last_index[i] = -1;
                s->dsp.clear_block(s->block[i]);
            }
        }
    } else {
        for (i = 0; i < 6; i++) {
            if (s->block_last_index[i] >= 0)
                cbp |= 1 << (5 - i);
        }
    }
    return cbp;
}

//FIXME this is duplicated to h263.c
static const int dquant_code[5]= {1,0,9,2,3};

/**
 * Skip the given number of bits.
 * Must only be used if the actual values in the bitstream do not matter.
 * If n is 0 the behavior is undefined.
 */
static inline void skip_put_bits(PutBitContext *s, int n)
{
    s->bit_left -= n;
    s->buf_ptr  -= 4 * (s->bit_left >> 5);
    s->bit_left &= 31;
}


static inline int h263_get_motion_length(MpegEncContext * s, int val, int f_code){
    int l, bit_size, code;

    if (val == 0) {
        return ff_mvtab[0][1];
    } else {
        bit_size = f_code - 1;
        /* modulo encoding */
        l= INT_BIT - 6 - bit_size;
        val = (val<<l)>>l;
        val--;
        code = (val >> bit_size) + 1;

        return ff_mvtab[code][1] + 1 + bit_size;
    }
}

static inline int sign_extend(int val, unsigned bits)
{
    unsigned shift = 8 * sizeof(int) - bits;
    union { unsigned u; int s; } v = { (unsigned) val << shift };
    return v.s >> shift;
}

void ff_h263_encode_motion(MpegEncContext * s, int val, int f_code)
{
    int range, bit_size, sign, code, bits;

    if (val == 0) {
        /* zero vector */
        code = 0;
        put_bits(&s->pb, ff_mvtab[code][1], ff_mvtab[code][0]);
    } else {
        bit_size = f_code - 1;
        range = 1 << bit_size;
        /* modulo encoding */
        val = sign_extend(val, 6 + bit_size);
        sign = val>>31;
        val= (val^sign)-sign;
        sign&=1;

        val--;
        code = (val >> bit_size) + 1;
        bits = val & (range - 1);

        put_bits(&s->pb, ff_mvtab[code][1] + 1, (ff_mvtab[code][0] << 1) | sign);
        if (bit_size > 0) {
            put_bits(&s->pb, bit_size, bits);
        }
    }
}

static inline void ff_h263_encode_motion_vector(MpegEncContext * s, int x, int y, int f_code){
    if(s->flags2 & CODEC_FLAG2_NO_OUTPUT){
        skip_put_bits(&s->pb,
            h263_get_motion_length(s, x, f_code)
           +h263_get_motion_length(s, y, f_code));
    }else{
        ff_h263_encode_motion(s, x, f_code);
        ff_h263_encode_motion(s, y, f_code);
    }
}


static inline int mpeg4_get_dc_length(int level, int n){
    if (n < 4) {
        return uni_DCtab_lum_len[level + 256];
    } else {
        return uni_DCtab_chrom_len[level + 256];
    }
}

static int mpeg4_get_block_length(MpegEncContext * s, int16_t * block, int n, int intra_dc,
                               uint8_t *scan_table)
{
    int i, last_non_zero;
    uint8_t *len_tab;
    const int last_index = s->block_last_index[n];
    int len=0;

    if (s->mb_intra) { //Note gcc (3.2.1 at least) will optimize this away
        /* mpeg4 based DC predictor */
        len += mpeg4_get_dc_length(intra_dc, n);
        if(last_index<1) return len;
        i = 1;
        len_tab = uni_mpeg4_intra_rl_len;
    } else {
        if(last_index<0) return 0;
        i = 0;
        len_tab = uni_mpeg4_inter_rl_len;
    }

    /* AC coefs */
    last_non_zero = i - 1;
    for (; i < last_index; i++) {
        int level = block[ scan_table[i] ];
        if (level) {
            int run = i - last_non_zero - 1;
            level+=64;
            if((level&(~127)) == 0){
                const int index= UNI_MPEG4_ENC_INDEX(0, run, level);
                len += len_tab[index];
            }else{ //ESC3
                len += 7+2+1+6+1+12+1;
            }
            last_non_zero = i;
        }
    }
    /*if(i<=last_index)*/{
        int level = block[ scan_table[i] ];
        int run = i - last_non_zero - 1;
        level+=64;
        if((level&(~127)) == 0){
            const int index= UNI_MPEG4_ENC_INDEX(1, run, level);
            len += len_tab[index];
        }else{ //ESC3
            len += 7+2+1+6+1+12+1;
        }
    }

    return len;
}



/**
 * Encode the dc value.
 * @param n block index (0-3 are luma, 4-5 are chroma)
 */
static inline void mpeg4_encode_dc(PutBitContext * s, int level, int n)
{
#if 1
    /* DC will overflow if level is outside the [-255,255] range. */
    level+=256;
    if (n < 4) {
        /* luminance */
        put_bits(s, uni_DCtab_lum_len[level], uni_DCtab_lum_bits[level]);
    } else {
        /* chrominance */
        put_bits(s, uni_DCtab_chrom_len[level], uni_DCtab_chrom_bits[level]);
    }
#else
    int size, v;
    /* find number of bits */
    size = 0;
    v = abs(level);
    while (v) {
        v >>= 1;
        size++;
    }

    if (n < 4) {
        /* luminance */
        put_bits(&s->pb, ff_mpeg4_DCtab_lum[size][1], ff_mpeg4_DCtab_lum[size][0]);
    } else {
        /* chrominance */
        put_bits(&s->pb, ff_mpeg4_DCtab_chrom[size][1], ff_mpeg4_DCtab_chrom[size][0]);
    }

    /* encode remaining bits */
    if (size > 0) {
        if (level < 0)
            level = (-level) ^ ((1 << size) - 1);
        put_bits(&s->pb, size, level);
        if (size > 8)
            put_bits(&s->pb, 1, 1);
    }
#endif
}

/**
 * Encode an 8x8 block.
 * @param n block index (0-3 are luma, 4-5 are chroma)
 */
static inline void mpeg4_encode_block(MpegEncContext * s, int16_t * block, int n, int intra_dc,
                               uint8_t *scan_table, PutBitContext *dc_pb, PutBitContext *ac_pb)
{
    int i, last_non_zero;
    uint32_t *bits_tab;
    uint8_t *len_tab;
    const int last_index = s->block_last_index[n];

    if (s->mb_intra) { //Note gcc (3.2.1 at least) will optimize this away
        /* mpeg4 based DC predictor */
        mpeg4_encode_dc(dc_pb, intra_dc, n);
        if(last_index<1) return;
        i = 1;
        bits_tab= uni_mpeg4_intra_rl_bits;
        len_tab = uni_mpeg4_intra_rl_len;
    } else {
        if(last_index<0) return;
        i = 0;
        bits_tab= uni_mpeg4_inter_rl_bits;
        len_tab = uni_mpeg4_inter_rl_len;
    }

    /* AC coefs */
    last_non_zero = i - 1;
    for (; i < last_index; i++) {
        int level = block[ scan_table[i] ];
        if (level) {
            int run = i - last_non_zero - 1;
            level+=64;
            if((level&(~127)) == 0){
                const int index= UNI_MPEG4_ENC_INDEX(0, run, level);
                put_bits(ac_pb, len_tab[index], bits_tab[index]);
            }else{ //ESC3
                put_bits(ac_pb, 7+2+1+6+1+12+1, (3<<23)+(3<<21)+(0<<20)+(run<<14)+(1<<13)+(((level-64)&0xfff)<<1)+1);
            }
            last_non_zero = i;
        }
    }
    /*if(i<=last_index)*/{
        int level = block[ scan_table[i] ];
        int run = i - last_non_zero - 1;
        level+=64;
        if((level&(~127)) == 0){
            const int index= UNI_MPEG4_ENC_INDEX(1, run, level);
            put_bits(ac_pb, len_tab[index], bits_tab[index]);
        }else{ //ESC3
            put_bits(ac_pb, 7+2+1+6+1+12+1, (3<<23)+(3<<21)+(1<<20)+(run<<14)+(1<<13)+(((level-64)&0xfff)<<1)+1);
        }
    }
}


static inline void mpeg4_encode_blocks(MpegEncContext * s, int16_t block[6][64], int intra_dc[6],
                               uint8_t **scan_table, PutBitContext *dc_pb, PutBitContext *ac_pb){
    int i;

    if(scan_table){
        if(s->flags2 & CODEC_FLAG2_NO_OUTPUT){
            for (i = 0; i < 6; i++) {
                skip_put_bits(&s->pb, mpeg4_get_block_length(s, block[i], i, intra_dc[i], scan_table[i]));
            }
        }else{
            /* encode each block */
            for (i = 0; i < 6; i++) {
                mpeg4_encode_block(s, block[i], i, intra_dc[i], scan_table[i], dc_pb, ac_pb);
            }
        }
    }else{
        if(s->flags2 & CODEC_FLAG2_NO_OUTPUT){
            for (i = 0; i < 6; i++) {
                skip_put_bits(&s->pb, mpeg4_get_block_length(s, block[i], i, 0, s->intra_scantable.permutated));
            }
        }else{
            /* encode each block */
            for (i = 0; i < 6; i++) {
                mpeg4_encode_block(s, block[i], i, 0, s->intra_scantable.permutated, dc_pb, ac_pb);
            }
        }
    }
}

static inline int get_p_cbp(MpegEncContext * s,
                      int16_t block[6][64],
                      int motion_x, int motion_y){
    int cbp, i;

    if (s->mpv_flags & FF_MPV_FLAG_CBP_RD) {
        int best_cbpy_score= INT_MAX;
        int best_cbpc_score= INT_MAX;
        int cbpc = (-1), cbpy= (-1);
        const int offset= (s->mv_type==MV_TYPE_16X16 ? 0 : 16) + (s->dquant ? 8 : 0);
        const int lambda= s->lambda2 >> (FF_LAMBDA_SHIFT - 6);

        for(i=0; i<4; i++){
            int score= ff_h263_inter_MCBPC_bits[i + offset] * lambda;
            if(i&1) score += s->coded_score[5];
            if(i&2) score += s->coded_score[4];

            if(score < best_cbpc_score){
                best_cbpc_score= score;
                cbpc= i;
            }
        }

        for(i=0; i<16; i++){
            int score= ff_h263_cbpy_tab[i ^ 0xF][1] * lambda;
            if(i&1) score += s->coded_score[3];
            if(i&2) score += s->coded_score[2];
            if(i&4) score += s->coded_score[1];
            if(i&8) score += s->coded_score[0];

            if(score < best_cbpy_score){
                best_cbpy_score= score;
                cbpy= i;
            }
        }
        cbp= cbpc + 4*cbpy;
        if ((motion_x | motion_y | s->dquant) == 0 && s->mv_type==MV_TYPE_16X16){
            if(best_cbpy_score + best_cbpc_score + 2*lambda >= 0)
                cbp= 0;
        }

        for (i = 0; i < 6; i++) {
            if (s->block_last_index[i] >= 0 && ((cbp >> (5 - i))&1)==0 ){
                s->block_last_index[i]= -1;
                s->dsp.clear_block(s->block[i]);
            }
        }
    }else{
        cbp= 0;
        for (i = 0; i < 6; i++) {
            if (s->block_last_index[i] >= 0)
                cbp |= 1 << (5 - i);
        }
    }
    return cbp;
}


/**
 * Predict the dc.
 * encoding quantized level -> quantized diff
 * decoding quantized diff -> quantized level
 * @param n block index (0-3 are luma, 4-5 are chroma)
 * @param dir_ptr pointer to an integer where the prediction direction will be stored
 */
static inline int ff_mpeg4_pred_dc(MpegEncContext * s, int n, int level, int *dir_ptr, int encoding)
{
    int a, b, c, wrap, pred, scale, ret;
    int16_t *dc_val;

    /* find prediction */
    if (n < 4) {
        scale = s->y_dc_scale;
    } else {
        scale = s->c_dc_scale;
    }
    if(IS_3IV1)
        scale= 8;

    wrap= s->block_wrap[n];
    dc_val = s->dc_val[0] + s->block_index[n];

    /* B C
     * A X
     */
    a = dc_val[ - 1];
    b = dc_val[ - 1 - wrap];
    c = dc_val[ - wrap];

    /* outside slice handling (we can't do that by memset as we need the dc for error resilience) */
    if(s->first_slice_line && n!=3){
        if(n!=2) b=c= 1024;
        if(n!=1 && s->mb_x == s->resync_mb_x) b=a= 1024;
    }
    if(s->mb_x == s->resync_mb_x && s->mb_y == s->resync_mb_y+1){
        if(n==0 || n==4 || n==5)
            b=1024;
    }

    if (abs(a - b) < abs(b - c)) {
        pred = c;
        *dir_ptr = 1; /* top */
    } else {
        pred = a;
        *dir_ptr = 0; /* left */
    }
    /* we assume pred is positive */
    pred = FASTDIV((pred + (scale >> 1)), scale);

    if(encoding){
        ret = level - pred;
    }else{
        level += pred;
        ret= level;
        if(s->err_recognition&(AV_EF_BITSTREAM|AV_EF_AGGRESSIVE)){
            if(level<0){
                printf( "dc<0 at %dx%d\n", s->mb_x, s->mb_y);
                return -1;
            }
            if(level*scale > 2048 + scale){
                printf( "dc overflow at %dx%d\n", s->mb_x, s->mb_y);
                return -1;
            }
        }
    }
    level *=scale;
    if(level&(~2047)){
        if(level<0)
            level=0;
        else if(!(s->workaround_bugs&FF_BUG_DC_CLIP))
            level=2047;
    }
    dc_val[0]= level;

    return ret;
}

/**
 * Restore the ac coefficients in block that have been changed by decide_ac_pred().
 * This function also restores s->block_last_index.
 * @param[in,out] block MB coefficients, these will be restored
 * @param[in] dir ac prediction direction for each 8x8 block
 * @param[out] st scantable for each 8x8 block
 * @param[in] zigzag_last_index index referring to the last non zero coefficient in zigzag order
 */
static inline void restore_ac_coeffs(MpegEncContext * s, int16_t block[6][64], const int dir[6], uint8_t *st[6], const int zigzag_last_index[6])
{
    int i, n;
    memcpy(s->block_last_index, zigzag_last_index, sizeof(int)*6);

    for(n=0; n<6; n++){
        int16_t *ac_val = s->ac_val[0][0] + s->block_index[n] * 16;

        st[n]= s->intra_scantable.permutated;
        if(dir[n]){
            /* top prediction */
            for(i=1; i<8; i++){
                block[n][s->dsp.idct_permutation[i   ]] = ac_val[i+8];
            }
        }else{
            /* left prediction */
            for(i=1; i<8; i++){
                block[n][s->dsp.idct_permutation[i<<3]]= ac_val[i  ];
            }
        }
    }
}



/**
 * Return the number of bits that encoding the 8x8 block in block would need.
 * @param[in]  block_last_index last index in scantable order that refers to a non zero element in block.
 */
static inline int get_block_rate(MpegEncContext * s, int16_t block[64], int block_last_index, uint8_t scantable[64]){

	LOGD("get_block_rate block_last_index\n");
	int last=0;
    int j;
    int rate=0;

    for(j=1; j<=block_last_index; j++){
        const int index= scantable[j];
        int level= block[index];
        if(level){
            level+= 64;
            if((level&(~127)) == 0){
                if(j<block_last_index) rate+= s->intra_ac_vlc_length     [UNI_AC_ENC_INDEX(j-last-1, level)];
                else                   rate+= s->intra_ac_vlc_last_length[UNI_AC_ENC_INDEX(j-last-1, level)];
            }else
                rate += s->ac_esc_length;

            last= j;
        }
    }

    return rate;
}

/**
 * Return the optimal value (0 or 1) for the ac_pred element for the given MB in mpeg4.
 * This function will also update s->block_last_index and s->ac_val.
 * @param[in,out] block MB coefficients, these will be updated if 1 is returned
 * @param[in] dir ac prediction direction for each 8x8 block
 * @param[out] st scantable for each 8x8 block
 * @param[out] zigzag_last_index index referring to the last non zero coefficient in zigzag order
 */
static inline int decide_ac_pred(MpegEncContext * s, int16_t block[6][64], const int dir[6], uint8_t *st[6], int zigzag_last_index[6])
{
	LOGD("decide_ac_pred block_last_index\n");
    int score= 0;
    int i, n;
    int8_t * const qscale_table = s->current_picture.f.qscale_table;

    memcpy(zigzag_last_index, s->block_last_index, sizeof(int)*6);

    for(n=0; n<6; n++){
        int16_t *ac_val, *ac_val1;

        score -= get_block_rate(s, block[n], s->block_last_index[n], s->intra_scantable.permutated);

        ac_val = s->ac_val[0][0] + s->block_index[n] * 16;
        ac_val1= ac_val;
        if(dir[n]){
            const int xy= s->mb_x + s->mb_y*s->mb_stride - s->mb_stride;
            /* top prediction */
            ac_val-= s->block_wrap[n]*16;
            if(s->mb_y==0 || s->qscale == qscale_table[xy] || n==2 || n==3){
                /* same qscale */
                for(i=1; i<8; i++){
                    const int level= block[n][s->dsp.idct_permutation[i   ]];
                    block[n][s->dsp.idct_permutation[i   ]] = level - ac_val[i+8];
                    ac_val1[i  ]=    block[n][s->dsp.idct_permutation[i<<3]];
                    ac_val1[i+8]= level;
                }
            }else{
                /* different qscale, we must rescale */
                for(i=1; i<8; i++){
                    const int level= block[n][s->dsp.idct_permutation[i   ]];
                    block[n][s->dsp.idct_permutation[i   ]] = level - ROUNDED_DIV(ac_val[i + 8]*qscale_table[xy], s->qscale);
                    ac_val1[i  ]=    block[n][s->dsp.idct_permutation[i<<3]];
                    ac_val1[i+8]= level;
                }
            }
            st[n]= s->intra_h_scantable.permutated;
        }else{
            const int xy= s->mb_x-1 + s->mb_y*s->mb_stride;
            /* left prediction */
            ac_val-= 16;
            if(s->mb_x==0 || s->qscale == qscale_table[xy] || n==1 || n==3){
                /* same qscale */
                for(i=1; i<8; i++){
                    const int level= block[n][s->dsp.idct_permutation[i<<3]];
                    block[n][s->dsp.idct_permutation[i<<3]]= level - ac_val[i];
                    ac_val1[i  ]= level;
                    ac_val1[i+8]=    block[n][s->dsp.idct_permutation[i   ]];
                }
            }else{
                /* different qscale, we must rescale */
                for(i=1; i<8; i++){
                    const int level= block[n][s->dsp.idct_permutation[i<<3]];
                    block[n][s->dsp.idct_permutation[i<<3]]= level - ROUNDED_DIV(ac_val[i]*qscale_table[xy], s->qscale);
                    ac_val1[i  ]= level;
                    ac_val1[i+8]=    block[n][s->dsp.idct_permutation[i   ]];
                }
            }
            st[n]= s->intra_v_scantable.permutated;
        }

        for(i=63; i>0; i--) //FIXME optimize
            if(block[n][ st[n][i] ]) break;
        s->block_last_index[n]= i;

        score += get_block_rate(s, block[n], s->block_last_index[n], st[n]);
    }

    if(score < 0){
        return 1;
    }else{
        restore_ac_coeffs(s, block, dir, st, zigzag_last_index);
        return 0;
    }
}

void ff_mpeg4_encode_mb(MpegEncContext * s,
                        int16_t block[6][64],
                        int motion_x, int motion_y)
{
    int cbpc, cbpy, pred_x, pred_y;
    PutBitContext * const pb2    = s->data_partitioning                         ? &s->pb2    : &s->pb;
    PutBitContext * const tex_pb = s->data_partitioning && s->pict_type!=AV_PICTURE_TYPE_B ? &s->tex_pb : &s->pb;
    PutBitContext * const dc_pb  = s->data_partitioning && s->pict_type!=AV_PICTURE_TYPE_I ? &s->pb2    : &s->pb;
    const int interleaved_stats= (s->flags&CODEC_FLAG_PASS1) && !s->data_partitioning ? 1 : 0;

    if (!s->mb_intra) {
        int i, cbp;

        if(s->pict_type==AV_PICTURE_TYPE_B){
            static const int mb_type_table[8]= {-1, 3, 2, 1,-1,-1,-1, 0}; /* convert from mv_dir to type */
            int mb_type=  mb_type_table[s->mv_dir];

            if(s->mb_x==0){
                for(i=0; i<2; i++){
                    s->last_mv[i][0][0]=
                    s->last_mv[i][0][1]=
                    s->last_mv[i][1][0]=
                    s->last_mv[i][1][1]= 0;
                }
            }

//            av_assert2(s->dquant>=-2 && s->dquant<=2);
//            av_assert2((s->dquant&1)==0);
//            av_assert2(mb_type>=0);

            /* nothing to do if this MB was skipped in the next P Frame */
            if (s->next_picture.f.mbskip_table[s->mb_y * s->mb_stride + s->mb_x]) { //FIXME avoid DCT & ...
                s->skip_count++;
                s->mv[0][0][0]=
                s->mv[0][0][1]=
                s->mv[1][0][0]=
                s->mv[1][0][1]= 0;
                s->mv_dir= MV_DIR_FORWARD; //doesn't matter
                s->qscale -= s->dquant;
//                s->mb_skipped=1;

                return;
            }

            cbp= get_b_cbp(s, block, motion_x, motion_y, mb_type);

            if ((cbp | motion_x | motion_y | mb_type) ==0) {
                /* direct MB with MV={0,0} */
//                av_assert2(s->dquant==0);

                put_bits(&s->pb, 1, 1); /* mb not coded modb1=1 */

                if(interleaved_stats){
                    s->misc_bits++;
                    s->last_bits++;
                }
                s->skip_count++;
                return;
            }

            put_bits(&s->pb, 1, 0);     /* mb coded modb1=0 */
            put_bits(&s->pb, 1, cbp ? 0 : 1); /* modb2 */ //FIXME merge
            put_bits(&s->pb, mb_type+1, 1); // this table is so simple that we don't need it :)
            if(cbp) put_bits(&s->pb, 6, cbp);

            if(cbp && mb_type){
                if(s->dquant)
                    put_bits(&s->pb, 2, (s->dquant>>2)+3);
                else
                    put_bits(&s->pb, 1, 0);
            }else
                s->qscale -= s->dquant;

            if(!s->progressive_sequence){
                if(cbp)
                    put_bits(&s->pb, 1, s->interlaced_dct);
                if(mb_type) // not direct mode
                    put_bits(&s->pb, 1, s->mv_type == MV_TYPE_FIELD);
            }

            if(interleaved_stats){
                s->misc_bits+= get_bits_diff(s);
            }

            if(mb_type == 0){
//                av_assert2(s->mv_dir & MV_DIRECT);
                ff_h263_encode_motion_vector(s, motion_x, motion_y, 1);
                s->b_count++;
                s->f_count++;
            }else{
//                av_assert2(mb_type > 0 && mb_type < 4);
                if(s->mv_type != MV_TYPE_FIELD){
                    if(s->mv_dir & MV_DIR_FORWARD){
                        ff_h263_encode_motion_vector(s, s->mv[0][0][0] - s->last_mv[0][0][0],
                                                        s->mv[0][0][1] - s->last_mv[0][0][1], s->f_code);
                        s->last_mv[0][0][0]= s->last_mv[0][1][0]= s->mv[0][0][0];
                        s->last_mv[0][0][1]= s->last_mv[0][1][1]= s->mv[0][0][1];
                        s->f_count++;
                    }
                    if(s->mv_dir & MV_DIR_BACKWARD){
                        ff_h263_encode_motion_vector(s, s->mv[1][0][0] - s->last_mv[1][0][0],
                                                        s->mv[1][0][1] - s->last_mv[1][0][1], s->b_code);
                        s->last_mv[1][0][0]= s->last_mv[1][1][0]= s->mv[1][0][0];
                        s->last_mv[1][0][1]= s->last_mv[1][1][1]= s->mv[1][0][1];
                        s->b_count++;
                    }
                }else{
                    if(s->mv_dir & MV_DIR_FORWARD){
                        put_bits(&s->pb, 1, s->field_select[0][0]);
                        put_bits(&s->pb, 1, s->field_select[0][1]);
                    }
                    if(s->mv_dir & MV_DIR_BACKWARD){
                        put_bits(&s->pb, 1, s->field_select[1][0]);
                        put_bits(&s->pb, 1, s->field_select[1][1]);
                    }
                    if(s->mv_dir & MV_DIR_FORWARD){
                        for(i=0; i<2; i++){
                            ff_h263_encode_motion_vector(s, s->mv[0][i][0] - s->last_mv[0][i][0]  ,
                                                            s->mv[0][i][1] - s->last_mv[0][i][1]/2, s->f_code);
                            s->last_mv[0][i][0]= s->mv[0][i][0];
                            s->last_mv[0][i][1]= s->mv[0][i][1]*2;
                        }
                        s->f_count++;
                    }
                    if(s->mv_dir & MV_DIR_BACKWARD){
                        for(i=0; i<2; i++){
                            ff_h263_encode_motion_vector(s, s->mv[1][i][0] - s->last_mv[1][i][0]  ,
                                                            s->mv[1][i][1] - s->last_mv[1][i][1]/2, s->b_code);
                            s->last_mv[1][i][0]= s->mv[1][i][0];
                            s->last_mv[1][i][1]= s->mv[1][i][1]*2;
                        }
                        s->b_count++;
                    }
                }
            }

            if(interleaved_stats){
                s->mv_bits+= get_bits_diff(s);
            }

            mpeg4_encode_blocks(s, block, NULL, NULL, NULL, &s->pb);

            if(interleaved_stats){
                s->p_tex_bits+= get_bits_diff(s);
            }

        }else{ /* s->pict_type==AV_PICTURE_TYPE_B */
            cbp= get_p_cbp(s, block, motion_x, motion_y);

            if ((cbp | motion_x | motion_y | s->dquant) == 0 && s->mv_type==MV_TYPE_16X16) {
                /* check if the B frames can skip it too, as we must skip it if we skip here
                   why didn't they just compress the skip-mb bits instead of reusing them ?! */
                if(s->max_b_frames>0){
                    int i;
                    int x,y, offset;
                    uint8_t *p_pic;

                    x= s->mb_x*16;
                    y= s->mb_y*16;

                    offset= x + y*s->linesize;
                    p_pic = s->new_picture.f.data[0] + offset;

                    s->mb_skipped=1;
                    for(i=0; i<s->max_b_frames; i++){
                        uint8_t *b_pic;
                        int diff;
                        Picture *pic= s->reordered_input_picture[i+1];

                        if (pic == NULL || pic->f.pict_type != AV_PICTURE_TYPE_B)
                            break;

                        b_pic = pic->f.data[0] + offset;
                        if (pic->f.type != FF_BUFFER_TYPE_SHARED)
                            b_pic+= INPLACE_OFFSET;

                        if(x+16 > s->width || y+16 > s->height){
                            int x1,y1;
                            int xe= FFMIN(16, s->width - x);
                            int ye= FFMIN(16, s->height- y);
                            diff=0;
                            for(y1=0; y1<ye; y1++){
                                for(x1=0; x1<xe; x1++){
                                    diff+= FFABS(p_pic[x1+y1*s->linesize] - b_pic[x1+y1*s->linesize]);
                                }
                            }
                            diff= diff*256/(xe*ye);
                        }else{
                            diff= s->dsp.sad[0](NULL, p_pic, b_pic, s->linesize, 16);
                        }
                        if(diff>s->qscale*70){ //FIXME check that 70 is optimal
                            s->mb_skipped=0;
                            break;
                        }
                    }
                }else
                    s->mb_skipped=1;

                if(s->mb_skipped==1){
                    /* skip macroblock */
                    put_bits(&s->pb, 1, 1);

                    if(interleaved_stats){
                        s->misc_bits++;
                        s->last_bits++;
                    }
                    s->skip_count++;

                    return;
                }
            }

            put_bits(&s->pb, 1, 0);     /* mb coded */
            cbpc = cbp & 3;
            cbpy = cbp >> 2;
            cbpy ^= 0xf;
            if(s->mv_type==MV_TYPE_16X16){
                if(s->dquant) cbpc+= 8;
                put_bits(&s->pb,
                        ff_h263_inter_MCBPC_bits[cbpc],
                        ff_h263_inter_MCBPC_code[cbpc]);

                put_bits(pb2, ff_h263_cbpy_tab[cbpy][1], ff_h263_cbpy_tab[cbpy][0]);
                if(s->dquant)
                    put_bits(pb2, 2, dquant_code[s->dquant+2]);

                if(!s->progressive_sequence){
                    if(cbp)
                        put_bits(pb2, 1, s->interlaced_dct);
                    put_bits(pb2, 1, 0);
                }

                if(interleaved_stats){
                    s->misc_bits+= get_bits_diff(s);
                }

                /* motion vectors: 16x16 mode */
                ff_h263_pred_motion(s, 0, 0, &pred_x, &pred_y);

                ff_h263_encode_motion_vector(s, motion_x - pred_x,
                                                motion_y - pred_y, s->f_code);
            }else if(s->mv_type==MV_TYPE_FIELD){
                if(s->dquant) cbpc+= 8;
                put_bits(&s->pb,
                        ff_h263_inter_MCBPC_bits[cbpc],
                        ff_h263_inter_MCBPC_code[cbpc]);

                put_bits(pb2, ff_h263_cbpy_tab[cbpy][1], ff_h263_cbpy_tab[cbpy][0]);
                if(s->dquant)
                    put_bits(pb2, 2, dquant_code[s->dquant+2]);

//                av_assert2(!s->progressive_sequence);
                if(cbp)
                    put_bits(pb2, 1, s->interlaced_dct);
                put_bits(pb2, 1, 1);

                if(interleaved_stats){
                    s->misc_bits+= get_bits_diff(s);
                }

                /* motion vectors: 16x8 interlaced mode */
                ff_h263_pred_motion(s, 0, 0, &pred_x, &pred_y);
                pred_y /=2;

                put_bits(&s->pb, 1, s->field_select[0][0]);
                put_bits(&s->pb, 1, s->field_select[0][1]);

                ff_h263_encode_motion_vector(s, s->mv[0][0][0] - pred_x,
                                                s->mv[0][0][1] - pred_y, s->f_code);
                ff_h263_encode_motion_vector(s, s->mv[0][1][0] - pred_x,
                                                s->mv[0][1][1] - pred_y, s->f_code);
            }else{
//                av_assert2(s->mv_type==MV_TYPE_8X8);
                put_bits(&s->pb,
                        ff_h263_inter_MCBPC_bits[cbpc+16],
                        ff_h263_inter_MCBPC_code[cbpc+16]);
                put_bits(pb2, ff_h263_cbpy_tab[cbpy][1], ff_h263_cbpy_tab[cbpy][0]);

                if(!s->progressive_sequence){
                    if(cbp)
                        put_bits(pb2, 1, s->interlaced_dct);
                }

                if(interleaved_stats){
                    s->misc_bits+= get_bits_diff(s);
                }

                for(i=0; i<4; i++){
                    /* motion vectors: 8x8 mode*/
                    ff_h263_pred_motion(s, i, 0, &pred_x, &pred_y);

                    ff_h263_encode_motion_vector(s, s->current_picture.f.motion_val[0][ s->block_index[i] ][0] - pred_x,
                                                    s->current_picture.f.motion_val[0][ s->block_index[i] ][1] - pred_y, s->f_code);
                }
            }

            if(interleaved_stats){
                s->mv_bits+= get_bits_diff(s);
            }

            mpeg4_encode_blocks(s, block, NULL, NULL, NULL, tex_pb);

            if(interleaved_stats){
                s->p_tex_bits+= get_bits_diff(s);
            }
            s->f_count++;
        }
    } else {
        int cbp;
        int dc_diff[6];   //dc values with the dc prediction subtracted
        int dir[6];  //prediction direction
        int zigzag_last_index[6];
        uint8_t *scan_table[6];
        int i;

        for(i=0; i<6; i++){
            dc_diff[i]= ff_mpeg4_pred_dc(s, i, block[i][0], &dir[i], 1);
        }

        if(s->flags & CODEC_FLAG_AC_PRED){
            s->ac_pred= decide_ac_pred(s, block, dir, scan_table, zigzag_last_index);
        }else{
            for(i=0; i<6; i++)
                scan_table[i]= s->intra_scantable.permutated;
        }

        /* compute cbp */
        cbp = 0;
        for (i = 0; i < 6; i++) {
            if (s->block_last_index[i] >= 1)
                cbp |= 1 << (5 - i);
        }

        cbpc = cbp & 3;
        if (s->pict_type == AV_PICTURE_TYPE_I) {
            if(s->dquant) cbpc+=4;
            put_bits(&s->pb,
                ff_h263_intra_MCBPC_bits[cbpc],
                ff_h263_intra_MCBPC_code[cbpc]);
        } else {
            if(s->dquant) cbpc+=8;
            put_bits(&s->pb, 1, 0);     /* mb coded */
            put_bits(&s->pb,
                ff_h263_inter_MCBPC_bits[cbpc + 4],
                ff_h263_inter_MCBPC_code[cbpc + 4]);
        }
        put_bits(pb2, 1, s->ac_pred);
        cbpy = cbp >> 2;
        put_bits(pb2, ff_h263_cbpy_tab[cbpy][1], ff_h263_cbpy_tab[cbpy][0]);
        if(s->dquant)
            put_bits(dc_pb, 2, dquant_code[s->dquant+2]);

        if(!s->progressive_sequence){
            put_bits(dc_pb, 1, s->interlaced_dct);
        }

        if(interleaved_stats){
            s->misc_bits+= get_bits_diff(s);
        }

        mpeg4_encode_blocks(s, block, dc_diff, scan_table, dc_pb, tex_pb);

        if(interleaved_stats){
            s->i_tex_bits+= get_bits_diff(s);
        }
        s->i_count++;

        /* restore ac coeffs & last_index stuff if we messed them up with the prediction */
        if(s->ac_pred)
            restore_ac_coeffs(s, block, dir, scan_table, zigzag_last_index);
    }
}


static inline void clip_coeffs(MpegEncContext *s, int16_t *block,
                               int last_index)
{
    int i;
    const int maxlevel = s->max_qcoeff;
    const int minlevel = s->min_qcoeff;
    int overflow = 0;

    if (s->mb_intra) {
        i = 1; // skip clipping of intra dc
    } else
        i = 0;

    for (; i <= last_index; i++) {
        const int j = s->intra_scantable.permutated[i];
        int level = block[j];

        if (level > maxlevel) {
            level = maxlevel;
            overflow++;
        } else if (level < minlevel) {
            level = minlevel;
            overflow++;
        }

        block[j] = level;
    }

    if (overflow && s->avctx->mb_decision == FF_MB_DECISION_SIMPLE)
        printf("warning, clipping %d dct coefficients to %d..%d\n",
               overflow, minlevel, maxlevel);
}

static inline void dct_single_coeff_elimination(MpegEncContext *s,
                                                int n, int threshold)
{
    static const char tab[64] = {
        3, 2, 2, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0
    };
    int score = 0;
    int run = 0;
    int i;
    int16_t *block = s->block[n];
    const int last_index = s->block_last_index[n];
    int skip_dc;

    if (threshold < 0) {
        skip_dc = 0;
        threshold = -threshold;
    } else
        skip_dc = 1;

    /* Are all we could set to zero already zero? */
    if (last_index <= skip_dc - 1)
        return;

    for (i = 0; i <= last_index; i++) {
        const int j = s->intra_scantable.permutated[i];
        const int level = FFABS(block[j]);
        if (level == 1) {
            if (skip_dc && i == 0)
                continue;
            score += tab[run];
            run = 0;
        } else if (level > 1) {
            return;
        } else {
            run++;
        }
    }
    if (score >= threshold)
        return;
    for (i = skip_dc; i <= last_index; i++) {
        const int j = s->intra_scantable.permutated[i];
        block[j] = 0;
    }
    if (block[0])
        s->block_last_index[n] = 0;
    else
        s->block_last_index[n] = -1;
}


/* Encode MV differences on H.263+ with Unrestricted MV mode */
static void h263p_encode_umotion(MpegEncContext * s, int val)
{
    short sval = 0;
    short i = 0;
    short n_bits = 0;
    short temp_val;
    int code = 0;
    int tcode;

    if ( val == 0)
        put_bits(&s->pb, 1, 1);
    else if (val == 1)
        put_bits(&s->pb, 3, 0);
    else if (val == -1)
        put_bits(&s->pb, 3, 2);
    else {

        sval = ((val < 0) ? (short)(-val):(short)val);
        temp_val = sval;

        while (temp_val != 0) {
            temp_val = temp_val >> 1;
            n_bits++;
        }

        i = n_bits - 1;
        while (i > 0) {
            tcode = (sval & (1 << (i-1))) >> (i-1);
            tcode = (tcode << 1) | 1;
            code = (code << 2) | tcode;
            i--;
        }
        code = ((code << 1) | (val < 0)) << 1;
        put_bits(&s->pb, (2*n_bits)+1, code);
    }
}

int ff_h263_pred_dc(MpegEncContext * s, int n, int16_t **dc_val_ptr)
{
    int x, y, wrap, a, c, pred_dc;
    int16_t *dc_val;

    /* find prediction */
    if (n < 4) {
        x = 2 * s->mb_x + (n & 1);
        y = 2 * s->mb_y + ((n & 2) >> 1);
        wrap = s->b8_stride;
        dc_val = s->dc_val[0];
    } else {
        x = s->mb_x;
        y = s->mb_y;
        wrap = s->mb_stride;
        dc_val = s->dc_val[n - 4 + 1];
    }
    /* B C
     * A X
     */
    a = dc_val[(x - 1) + (y) * wrap];
    c = dc_val[(x) + (y - 1) * wrap];

    /* No prediction outside GOB boundary */
    if(s->first_slice_line && n!=3){
        if(n!=2) c= 1024;
        if(n!=1 && s->mb_x == s->resync_mb_x) a= 1024;
    }
    /* just DC prediction */
    if (a != 1024 && c != 1024)
        pred_dc = (a + c) >> 1;
    else if (a != 1024)
        pred_dc = a;
    else
        pred_dc = c;

    /* we assume pred is positive */
    *dc_val_ptr = &dc_val[x + y * wrap];
    return pred_dc;
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

static inline void put_sbits(PutBitContext *pb, int n, int32_t value)
{
//    av_assert2(n >= 0 && n <= 31);

    put_bits(pb, n, value & ((1 << n) - 1));
}


void ff_flv2_encode_ac_esc(PutBitContext *pb, int slevel, int level, int run, int last){
    if(level < 64) { // 7-bit level
        put_bits(pb, 1, 0);
        put_bits(pb, 1, last);
        put_bits(pb, 6, run);

        put_sbits(pb, 7, slevel);
    } else {
        /* 11-bit level */
        put_bits(pb, 1, 1);
        put_bits(pb, 1, last);
        put_bits(pb, 6, run);

        put_sbits(pb, 11, slevel);
    }
}


/**
 * Encode an 8x8 block.
 * @param block the 8x8 block
 * @param n block index (0-3 are luma, 4-5 are chroma)
 */
static void h263_encode_block(MpegEncContext * s, int16_t * block, int n)
{
    int level, run, last, i, j, last_index, last_non_zero, sign, slevel, code;
    RLTable *rl;

    rl = &ff_h263_rl_inter;
    if (s->mb_intra && !s->h263_aic) {
        /* DC coef */
        level = block[0];
        /* 255 cannot be represented, so we clamp */
        if (level > 254) {
            level = 254;
            block[0] = 254;
        }
        /* 0 cannot be represented also */
        else if (level < 1) {
            level = 1;
            block[0] = 1;
        }
        if (level == 128) //FIXME check rv10
            put_bits(&s->pb, 8, 0xff);
        else
            put_bits(&s->pb, 8, level);
        i = 1;
    } else {
        i = 0;
        if (s->h263_aic && s->mb_intra)
            rl = &ff_rl_intra_aic;

        if(s->alt_inter_vlc && !s->mb_intra){
            int aic_vlc_bits=0;
            int inter_vlc_bits=0;
            int wrong_pos=-1;
            int aic_code;

            last_index = s->block_last_index[n];
            last_non_zero = i - 1;
            for (; i <= last_index; i++) {
                j = s->intra_scantable.permutated[i];
                level = block[j];
                if (level) {
                    run = i - last_non_zero - 1;
                    last = (i == last_index);

                    if(level<0) level= -level;

                    code = get_rl_index(rl, last, run, level);
                    aic_code = get_rl_index(&ff_rl_intra_aic, last, run, level);
                    inter_vlc_bits += rl->table_vlc[code][1]+1;
                    aic_vlc_bits   += ff_rl_intra_aic.table_vlc[aic_code][1]+1;

                    if (code == rl->n) {
                        inter_vlc_bits += 1+6+8-1;
                    }
                    if (aic_code == ff_rl_intra_aic.n) {
                        aic_vlc_bits += 1+6+8-1;
                        wrong_pos += run + 1;
                    }else
                        wrong_pos += wrong_run[aic_code];
                    last_non_zero = i;
                }
            }
            i = 0;
            if(aic_vlc_bits < inter_vlc_bits && wrong_pos > 63)
                rl = &ff_rl_intra_aic;
        }
    }

    /* AC coefs */
    last_index = s->block_last_index[n];
    last_non_zero = i - 1;
    for (; i <= last_index; i++) {
        j = s->intra_scantable.permutated[i];
        level = block[j];
        if (level) {
            run = i - last_non_zero - 1;
            last = (i == last_index);
            sign = 0;
            slevel = level;
            if (level < 0) {
                sign = 1;
                level = -level;
            }
            code = get_rl_index(rl, last, run, level);
            put_bits(&s->pb, rl->table_vlc[code][1], rl->table_vlc[code][0]);
            if (code == rl->n) {
              if(!CONFIG_FLV_ENCODER || s->h263_flv <= 1){
                put_bits(&s->pb, 1, last);
                put_bits(&s->pb, 6, run);

//                av_assert2(slevel != 0);

                if(level < 128)
                    put_sbits(&s->pb, 8, slevel);
                else{
                    put_bits(&s->pb, 8, 128);
                    put_sbits(&s->pb, 5, slevel);
                    put_sbits(&s->pb, 6, slevel>>5);
                }
              }else{
                    ff_flv2_encode_ac_esc(&s->pb, slevel, level, run, last);
              }
            } else {
                put_bits(&s->pb, 1, sign);
            }
            last_non_zero = i;
        }
    }
}

void ff_h263_encode_mb(MpegEncContext * s,
                       int16_t block[6][64],
                       int motion_x, int motion_y)
{
//	LOGD("ff_h263_encode_mb Called\n");
    int cbpc, cbpy, i, cbp, pred_x, pred_y;
    int16_t pred_dc;
    int16_t rec_intradc[6];
    int16_t *dc_ptr[6];
    const int interleaved_stats= (s->flags&CODEC_FLAG_PASS1);
    //mb_intraは1、つまり!mb_intraは0
    if (!s->mb_intra) {
        /* compute cbp */
        cbp= get_p_cbp(s, block, motion_x, motion_y);

        if ((cbp | motion_x | motion_y | s->dquant | (s->mv_type - MV_TYPE_16X16)) == 0) {
            /* skip macroblock */
            put_bits(&s->pb, 1, 1);
            if(interleaved_stats){
                s->misc_bits++;
                s->last_bits++;
            }
            s->skip_count++;

            return;
        }
        put_bits(&s->pb, 1, 0);         /* mb coded */

        cbpc = cbp & 3;
        cbpy = cbp >> 2;
        if(s->alt_inter_vlc==0 || cbpc!=3)
            cbpy ^= 0xF;
        if(s->dquant) cbpc+= 8;
        if(s->mv_type==MV_TYPE_16X16){
            put_bits(&s->pb,
                    ff_h263_inter_MCBPC_bits[cbpc],
                    ff_h263_inter_MCBPC_code[cbpc]);

            put_bits(&s->pb, ff_h263_cbpy_tab[cbpy][1], ff_h263_cbpy_tab[cbpy][0]);
            if(s->dquant)
                put_bits(&s->pb, 2, dquant_code[s->dquant+2]);

            if(interleaved_stats){
                s->misc_bits+= get_bits_diff(s);
            }

            /* motion vectors: 16x16 mode */
            ff_h263_pred_motion(s, 0, 0, &pred_x, &pred_y);

            if (!s->umvplus) {
                ff_h263_encode_motion_vector(s, motion_x - pred_x,
                                                motion_y - pred_y, 1);
            }
            else {
                h263p_encode_umotion(s, motion_x - pred_x);
                h263p_encode_umotion(s, motion_y - pred_y);
                if (((motion_x - pred_x) == 1) && ((motion_y - pred_y) == 1))
                    /* To prevent Start Code emulation */
                    put_bits(&s->pb,1,1);
            }
        }else{
            put_bits(&s->pb,
                    ff_h263_inter_MCBPC_bits[cbpc+16],
                    ff_h263_inter_MCBPC_code[cbpc+16]);
            put_bits(&s->pb, ff_h263_cbpy_tab[cbpy][1], ff_h263_cbpy_tab[cbpy][0]);
            if(s->dquant)
                put_bits(&s->pb, 2, dquant_code[s->dquant+2]);

            if(interleaved_stats){
                s->misc_bits+= get_bits_diff(s);
            }

            for(i=0; i<4; i++){
                /* motion vectors: 8x8 mode*/
                ff_h263_pred_motion(s, i, 0, &pred_x, &pred_y);

                motion_x = s->current_picture.f.motion_val[0][s->block_index[i]][0];
                motion_y = s->current_picture.f.motion_val[0][s->block_index[i]][1];
                if (!s->umvplus) {
                    ff_h263_encode_motion_vector(s, motion_x - pred_x,
                                                    motion_y - pred_y, 1);
                }
                else {
                    h263p_encode_umotion(s, motion_x - pred_x);
                    h263p_encode_umotion(s, motion_y - pred_y);
                    if (((motion_x - pred_x) == 1) && ((motion_y - pred_y) == 1))
                        /* To prevent Start Code emulation */
                        put_bits(&s->pb,1,1);
                }
            }
        }

        if(interleaved_stats){
            s->mv_bits+= get_bits_diff(s);
        }
    } else {//少なくとも初回はここを通る
//        av_assert2(s->mb_intra);

        cbp = 0;
        if (s->h263_aic) {
            /* Predict DC */
            for(i=0; i<6; i++) {
                int16_t level = block[i][0];
                int scale;

                if(i<4) scale= s->y_dc_scale;
                else    scale= s->c_dc_scale;

                pred_dc = ff_h263_pred_dc(s, i, &dc_ptr[i]);
                level -= pred_dc;
                /* Quant */
                if (level >= 0)
                    level = (level + (scale>>1))/scale;
                else
                    level = (level - (scale>>1))/scale;

                /* AIC can change CBP */
                if (level == 0 && s->block_last_index[i] == 0)
                    s->block_last_index[i] = -1;

                if(!s->modified_quant){
                    if (level < -127)
                        level = -127;
                    else if (level > 127)
                        level = 127;
                }

                block[i][0] = level;
                /* Reconstruction */
                rec_intradc[i] = scale*level + pred_dc;
                /* Oddify */
                rec_intradc[i] |= 1;
                //if ((rec_intradc[i] % 2) == 0)
                //    rec_intradc[i]++;
                /* Clipping */
                if (rec_intradc[i] < 0)
                    rec_intradc[i] = 0;
                else if (rec_intradc[i] > 2047)
                    rec_intradc[i] = 2047;

                /* Update AC/DC tables */
                *dc_ptr[i] = rec_intradc[i];
                if (s->block_last_index[i] >= 0)
                    cbp |= 1 << (5 - i);
            }
        }else{//h263_aic is 0
            for(i=0; i<6; i++) {
                /* compute cbp */
                if (s->block_last_index[i] >= 1)
                    cbp |= 1 << (5 - i);
           }
        }
        //cbp cbpcの値
        cbpc = cbp & 3;
        if (s->pict_type == AV_PICTURE_TYPE_I) {
            if(s->dquant) cbpc+=4;
            put_bits(&s->pb,
                ff_h263_intra_MCBPC_bits[cbpc],
                ff_h263_intra_MCBPC_code[cbpc]);
        } else {
            if(s->dquant) cbpc+=8;
            put_bits(&s->pb, 1, 0);     /* mb coded */
            put_bits(&s->pb,
                ff_h263_inter_MCBPC_bits[cbpc + 4],
                ff_h263_inter_MCBPC_code[cbpc + 4]);
        }
        if (s->h263_aic) {
            /* XXX: currently, we do not try to use ac prediction */
            put_bits(&s->pb, 1, 0);     /* no AC prediction */
        }
        cbpy = cbp >> 2;
        put_bits(&s->pb, ff_h263_cbpy_tab[cbpy][1], ff_h263_cbpy_tab[cbpy][0]);
        if(s->dquant)
            put_bits(&s->pb, 2, dquant_code[s->dquant+2]);

        if(interleaved_stats){
            s->misc_bits+= get_bits_diff(s);
        }
    }

    for(i=0; i<6; i++) {
        /* encode each block */
        h263_encode_block(s, block[i], i);

        /* Update INTRADC for decoding */
        if (s->h263_aic && s->mb_intra) {
            block[i][0] = rec_intradc[i];

        }
    }

    if(interleaved_stats){
        if (!s->mb_intra) {
            s->p_tex_bits+= get_bits_diff(s);
            s->f_count++;
        }else{
            s->i_tex_bits+= get_bits_diff(s);
            s->i_count++;
        }
    }
}
static int16_t basis[64][64];


static void build_basis(uint8_t *perm){
    int i, j, x, y;
    for(i=0; i<8; i++){
        for(j=0; j<8; j++){
            for(y=0; y<8; y++){
                for(x=0; x<8; x++){
                    double s= 0.25*(1<<BASIS_SHIFT);
                    int index= 8*i + j;
                    int perm_index= perm[index];
                    if(i==0) s*= sqrt(0.5);
                    if(j==0) s*= sqrt(0.5);
                    basis[perm_index][8*x + y]= lrintf(s * cos((M_PI/8.0)*i*(x+0.5)) * cos((M_PI/8.0)*j*(y+0.5)));
                }
            }
        }
    }
}

static int dct_quantize_refine(MpegEncContext *s, //FIXME breaks denoise?
                        int16_t *block, int16_t *weight, int16_t *orig,
                        int n, int qscale){
    int16_t rem[64];
    LOCAL_ALIGNED_16(int16_t, d1, [64]);
    const uint8_t *scantable= s->intra_scantable.scantable;
    const uint8_t *perm_scantable= s->intra_scantable.permutated;
//    unsigned int threshold1, threshold2;
//    int bias=0;
    int run_tab[65];
    int prev_run=0;
    int prev_level=0;
    int qmul, qadd, start_i, last_non_zero, i, dc;
    uint8_t * length;
    uint8_t * last_length;
    int lambda;
    int rle_index, run, q = 1, sum; //q is only used when s->mb_intra is true
#ifdef REFINE_STATS
static int count=0;
static int after_last=0;
static int to_zero=0;
static int from_zero=0;
static int raise=0;
static int lower=0;
static int messed_sign=0;
#endif

    if(basis[0][0] == 0)
        build_basis(s->dsp.idct_permutation);

    qmul= qscale*2;
    qadd= (qscale-1)|1;
    if (s->mb_intra) {
        if (!s->h263_aic) {
            if (n < 4)
                q = s->y_dc_scale;
            else
                q = s->c_dc_scale;
        } else{
            /* For AIC we skip quant/dequant of INTRADC */
            q = 1;
            qadd=0;
        }
        q <<= RECON_SHIFT-3;
        /* note: block[0] is assumed to be positive */
        dc= block[0]*q;
//        block[0] = (block[0] + (q >> 1)) / q;
        start_i = 1;
//        if(s->mpeg_quant || s->out_format == FMT_MPEG1)
//            bias= 1<<(QMAT_SHIFT-1);
        length     = s->intra_ac_vlc_length;
        last_length= s->intra_ac_vlc_last_length;
    } else {
        dc= 0;
        start_i = 0;
        length     = s->inter_ac_vlc_length;
        last_length= s->inter_ac_vlc_last_length;
    }
    last_non_zero = s->block_last_index[n];

#ifdef REFINE_STATS
{START_TIMER
#endif
    dc += (1<<(RECON_SHIFT-1));
    for(i=0; i<64; i++){
        rem[i]= dc - (orig[i]<<RECON_SHIFT); //FIXME  use orig dirrectly instead of copying to rem[]
    }
#ifdef REFINE_STATS
STOP_TIMER("memset rem[]")}
#endif
    sum=0;
    for(i=0; i<64; i++){
        int one= 36;
        int qns=4;
        int w;

        w= FFABS(weight[i]) + qns*one;
        w= 15 + (48*qns*one + w/2)/w; // 16 .. 63

        weight[i] = w;
//        w=weight[i] = (63*qns + (w/2)) / w;

//        av_assert2(w>0);
//        av_assert2(w<(1<<6));
        sum += w*w;
    }
    lambda= sum*(uint64_t)s->lambda2 >> (FF_LAMBDA_SHIFT - 6 + 6 + 6 + 6);
#ifdef REFINE_STATS
{START_TIMER
#endif
    run=0;
    rle_index=0;
    for(i=start_i; i<=last_non_zero; i++){
        int j= perm_scantable[i];
        const int level= block[j];
        int coeff;

        if(level){
            if(level<0) coeff= qmul*level - qadd;
            else        coeff= qmul*level + qadd;
            run_tab[rle_index++]=run;
            run=0;

            s->dsp.add_8x8basis(rem, basis[j], coeff);
        }else{
            run++;
        }
    }
#ifdef REFINE_STATS
if(last_non_zero>0){
STOP_TIMER("init rem[]")
}
}

{START_TIMER
#endif
    for(;;){
        int best_score=s->dsp.try_8x8basis(rem, weight, basis[0], 0);
        int best_coeff=0;
        int best_change=0;
        int run2, best_unquant_change=0, analyze_gradient;
#ifdef REFINE_STATS
{START_TIMER
#endif
        analyze_gradient = last_non_zero > 2 || s->quantizer_noise_shaping >= 3;

        if(analyze_gradient){
#ifdef REFINE_STATS
{START_TIMER
#endif
            for(i=0; i<64; i++){
                int w= weight[i];

                d1[i] = (rem[i]*w*w + (1<<(RECON_SHIFT+12-1)))>>(RECON_SHIFT+12);
            }
#ifdef REFINE_STATS
STOP_TIMER("rem*w*w")}
{START_TIMER
#endif
            s->dsp.fdct(d1);
#ifdef REFINE_STATS
STOP_TIMER("dct")}
#endif
        }

        if(start_i){
            const int level= block[0];
            int change, old_coeff;

//            av_assert2(s->mb_intra);

            old_coeff= q*level;

            for(change=-1; change<=1; change+=2){
                int new_level= level + change;
                int score, new_coeff;

                new_coeff= q*new_level;
                if(new_coeff >= 2048 || new_coeff < 0)
                    continue;

                score= s->dsp.try_8x8basis(rem, weight, basis[0], new_coeff - old_coeff);
                if(score<best_score){
                    best_score= score;
                    best_coeff= 0;
                    best_change= change;
                    best_unquant_change= new_coeff - old_coeff;
                }
            }
        }

        run=0;
        rle_index=0;
        run2= run_tab[rle_index++];
        prev_level=0;
        prev_run=0;

        for(i=start_i; i<64; i++){
            int j= perm_scantable[i];
            const int level= block[j];
            int change, old_coeff;

            if(s->quantizer_noise_shaping < 3 && i > last_non_zero + 1)
                break;

            if(level){
                if(level<0) old_coeff= qmul*level - qadd;
                else        old_coeff= qmul*level + qadd;
                run2= run_tab[rle_index++]; //FIXME ! maybe after last
            }else{
                old_coeff=0;
                run2--;
//                av_assert2(run2>=0 || i >= last_non_zero );
            }

            for(change=-1; change<=1; change+=2){
                int new_level= level + change;
                int score, new_coeff, unquant_change;

                score=0;
                if(s->quantizer_noise_shaping < 2 && FFABS(new_level) > FFABS(level))
                   continue;

                if(new_level){
                    if(new_level<0) new_coeff= qmul*new_level - qadd;
                    else            new_coeff= qmul*new_level + qadd;
                    if(new_coeff >= 2048 || new_coeff <= -2048)
                        continue;
                    //FIXME check for overflow

                    if(level){
                        if(level < 63 && level > -63){
                            if(i < last_non_zero)
                                score +=   length[UNI_AC_ENC_INDEX(run, new_level+64)]
                                         - length[UNI_AC_ENC_INDEX(run, level+64)];
                            else
                                score +=   last_length[UNI_AC_ENC_INDEX(run, new_level+64)]
                                         - last_length[UNI_AC_ENC_INDEX(run, level+64)];
                        }
                    }else{
//                        av_assert2(FFABS(new_level)==1);

                        if(analyze_gradient){
                            int g= d1[ scantable[i] ];
                            if(g && (g^new_level) >= 0)
                                continue;
                        }

                        if(i < last_non_zero){
                            int next_i= i + run2 + 1;
                            int next_level= block[ perm_scantable[next_i] ] + 64;

                            if(next_level&(~127))
                                next_level= 0;

                            if(next_i < last_non_zero)
                                score +=   length[UNI_AC_ENC_INDEX(run, 65)]
                                         + length[UNI_AC_ENC_INDEX(run2, next_level)]
                                         - length[UNI_AC_ENC_INDEX(run + run2 + 1, next_level)];
                            else
                                score +=  length[UNI_AC_ENC_INDEX(run, 65)]
                                        + last_length[UNI_AC_ENC_INDEX(run2, next_level)]
                                        - last_length[UNI_AC_ENC_INDEX(run + run2 + 1, next_level)];
                        }else{
                            score += last_length[UNI_AC_ENC_INDEX(run, 65)];
                            if(prev_level){
                                score +=  length[UNI_AC_ENC_INDEX(prev_run, prev_level)]
                                        - last_length[UNI_AC_ENC_INDEX(prev_run, prev_level)];
                            }
                        }
                    }
                }else{
                    new_coeff=0;
//                    av_assert2(FFABS(level)==1);

                    if(i < last_non_zero){
                        int next_i= i + run2 + 1;
                        int next_level= block[ perm_scantable[next_i] ] + 64;

                        if(next_level&(~127))
                            next_level= 0;

                        if(next_i < last_non_zero)
                            score +=   length[UNI_AC_ENC_INDEX(run + run2 + 1, next_level)]
                                     - length[UNI_AC_ENC_INDEX(run2, next_level)]
                                     - length[UNI_AC_ENC_INDEX(run, 65)];
                        else
                            score +=   last_length[UNI_AC_ENC_INDEX(run + run2 + 1, next_level)]
                                     - last_length[UNI_AC_ENC_INDEX(run2, next_level)]
                                     - length[UNI_AC_ENC_INDEX(run, 65)];
                    }else{
                        score += -last_length[UNI_AC_ENC_INDEX(run, 65)];
                        if(prev_level){
                            score +=  last_length[UNI_AC_ENC_INDEX(prev_run, prev_level)]
                                    - length[UNI_AC_ENC_INDEX(prev_run, prev_level)];
                        }
                    }
                }

                score *= lambda;

                unquant_change= new_coeff - old_coeff;
//                av_assert2((score < 100*lambda && score > -100*lambda) || lambda==0);

                score+= s->dsp.try_8x8basis(rem, weight, basis[j], unquant_change);
                if(score<best_score){
                    best_score= score;
                    best_coeff= i;
                    best_change= change;
                    best_unquant_change= unquant_change;
                }
            }
            if(level){
                prev_level= level + 64;
                if(prev_level&(~127))
                    prev_level= 0;
                prev_run= run;
                run=0;
            }else{
                run++;
            }
        }
#ifdef REFINE_STATS
STOP_TIMER("iterative step")}
#endif

        if(best_change){
            int j= perm_scantable[ best_coeff ];

            block[j] += best_change;

            if(best_coeff > last_non_zero){
                last_non_zero= best_coeff;
//                av_assert2(block[j]);
#ifdef REFINE_STATS
after_last++;
#endif
            }else{
#ifdef REFINE_STATS
if(block[j]){
    if(block[j] - best_change){
        if(FFABS(block[j]) > FFABS(block[j] - best_change)){
            raise++;
        }else{
            lower++;
        }
    }else{
        from_zero++;
    }
}else{
    to_zero++;
}
#endif
                for(; last_non_zero>=start_i; last_non_zero--){
                    if(block[perm_scantable[last_non_zero]])
                        break;
                }
            }
#ifdef REFINE_STATS
count++;
if(256*256*256*64 % count == 0){
    printf( "after_last:%d to_zero:%d from_zero:%d raise:%d lower:%d sign:%d xyp:%d/%d/%d\n", after_last, to_zero, from_zero, raise, lower, messed_sign, s->mb_x, s->mb_y, s->picture_number);
}
#endif
            run=0;
            rle_index=0;
            for(i=start_i; i<=last_non_zero; i++){
                int j= perm_scantable[i];
                const int level= block[j];

                 if(level){
                     run_tab[rle_index++]=run;
                     run=0;
                 }else{
                     run++;
                 }
            }

            s->dsp.add_8x8basis(rem, basis[j], best_unquant_change);
        }else{
            break;
        }
    }
#ifdef REFINE_STATS
if(last_non_zero>0){
STOP_TIMER("iterative search")
}
}
#endif

    return last_non_zero;
}

static inline void encode_mb_internal(MpegEncContext *s,
                                                int motion_x, int motion_y,
                                                int mb_block_height,
                                                int mb_block_width,
                                                int mb_block_count)
{
//	LOGD("encode_mb_internal Called m_x:%d m_y:%d m_b_h:%d m_b_w:%d m_b_c:%d\n",motion_x,motion_y,mb_block_height,mb_block_width,mb_block_count);
    int16_t weight[12][64];
    int16_t orig[12][64];
    const int mb_x = s->mb_x;
    const int mb_y = s->mb_y;
    int i;
    int skip_dct[12];
    int dct_offset = s->linesize * 8; // default for progressive frames
    int uv_dct_offset = s->uvlinesize * 8;
    uint8_t *ptr_y, *ptr_cb, *ptr_cr;
    int wrap_y, wrap_c;

    for (i = 0; i < mb_block_count; i++)
        skip_dct[i] = s->skipdct;

    if (s->adaptive_quant) {
        const int last_qp = s->qscale;
        const int mb_xy = mb_x + mb_y * s->mb_stride;

        s->lambda = s->lambda_table[mb_xy];
        update_qscale(s);

        if (!(s->mpv_flags & FF_MPV_FLAG_QP_RD)) {
            s->qscale = s->current_picture_ptr->f.qscale_table[mb_xy];
            s->dquant = s->qscale - last_qp;

            if (s->out_format == FMT_H263) {
                s->dquant = av_clip_c(s->dquant, -2, 2);

                if (s->codec_id == AV_CODEC_ID_MPEG4) {
                    if (!s->mb_intra) {
                        if (s->pict_type == AV_PICTURE_TYPE_B) {
                            if ((s->dquant & 1 )|| (s->mv_dir & MV_DIRECT))
                                s->dquant = 0;
                        }
                        if (s->mv_type == MV_TYPE_8X8)
                            s->dquant = 0;
                    }
                }
            }
        }
        ff_set_qscale(s, last_qp + s->dquant);
    } else if (s->mpv_flags & FF_MPV_FLAG_QP_RD)
        ff_set_qscale(s, s->qscale + s->dquant);

    wrap_y = s->linesize;
    wrap_c = s->uvlinesize;
    ptr_y  = s->new_picture.f.data[0] +
             (mb_y * 16 * wrap_y)              + mb_x * 16;
    ptr_cb = s->new_picture.f.data[1] +
             (mb_y * mb_block_height * wrap_c) + mb_x * mb_block_width;
    ptr_cr = s->new_picture.f.data[2] +
             (mb_y * mb_block_height * wrap_c) + mb_x * mb_block_width;

    if((mb_x*16+16 > s->width || mb_y*16+16 > s->height) && s->codec_id != AV_CODEC_ID_AMV){
    	LOGD("internal != AV_CODEC_ID_AMV) if\n");
    	uint8_t *ebuf = s->edge_emu_buffer + 32;
        int cw = (s->width  + s->chroma_x_shift) >> s->chroma_x_shift;
        int ch = (s->height + s->chroma_y_shift) >> s->chroma_y_shift;
        s->vdsp.emulated_edge_mc(ebuf, ptr_y, wrap_y, 16, 16, mb_x * 16,
                                 mb_y * 16, s->width, s->height);
        ptr_y = ebuf;
        s->vdsp.emulated_edge_mc(ebuf + 18 * wrap_y, ptr_cb, wrap_c, mb_block_width,
                                 mb_block_height, mb_x * mb_block_width, mb_y * mb_block_height,
                                 cw, ch);
        ptr_cb = ebuf + 18 * wrap_y;
        s->vdsp.emulated_edge_mc(ebuf + 18 * wrap_y + 16, ptr_cr, wrap_c, mb_block_width,
                                 mb_block_height, mb_x * mb_block_width, mb_y * mb_block_height,
                                 cw, ch);
        ptr_cr = ebuf + 18 * wrap_y + 16;
    }

	 if (s->mb_intra) {
        if (s->flags & CODEC_FLAG_INTERLACED_DCT) {
            int progressive_score, interlaced_score;

            s->interlaced_dct = 0;
            progressive_score = s->dsp.ildct_cmp[4](s, ptr_y,
                                                    NULL, wrap_y, 8) +
                                s->dsp.ildct_cmp[4](s, ptr_y + wrap_y * 8,
                                                    NULL, wrap_y, 8) - 400;

            if (progressive_score > 0) {
                interlaced_score = s->dsp.ildct_cmp[4](s, ptr_y,
                                                       NULL, wrap_y * 2, 8) +
                                   s->dsp.ildct_cmp[4](s, ptr_y + wrap_y,
                                                       NULL, wrap_y * 2, 8);
                if (progressive_score > interlaced_score) {
                    s->interlaced_dct = 1;

                    dct_offset = wrap_y;
                    uv_dct_offset = wrap_c;
                    wrap_y <<= 1;
                    if (s->chroma_format == CHROMA_422 ||
                        s->chroma_format == CHROMA_444)
                        wrap_c <<= 1;
                }
            }
        }

        s->dsp.get_pixels(s->block[0], ptr_y                  , wrap_y);
        s->dsp.get_pixels(s->block[1], ptr_y              + 8 , wrap_y);
        s->dsp.get_pixels(s->block[2], ptr_y + dct_offset     , wrap_y);
        s->dsp.get_pixels(s->block[3], ptr_y + dct_offset + 8 , wrap_y);

        if (s->flags & CODEC_FLAG_GRAY) {
            skip_dct[4] = 1;
            skip_dct[5] = 1;
        } else {
//        	LOGD("encode_mb_internal flag not GRAY block[3]:%d block[4]:%d block[5]:%d\n",s->block[3],s->block[4],s->block[5]);
            s->dsp.get_pixels(s->block[4], ptr_cb, wrap_c);
            s->dsp.get_pixels(s->block[5], ptr_cr, wrap_c);
            if (!s->chroma_y_shift && s->chroma_x_shift) { /* 422 */
                s->dsp.get_pixels(s->block[6], ptr_cb + uv_dct_offset, wrap_c);
                s->dsp.get_pixels(s->block[7], ptr_cr + uv_dct_offset, wrap_c);
            } else if (!s->chroma_y_shift && !s->chroma_x_shift) { /* 444 */
                s->dsp.get_pixels(s->block[6], ptr_cb + 8, wrap_c);
                s->dsp.get_pixels(s->block[7], ptr_cr + 8, wrap_c);
                s->dsp.get_pixels(s->block[8], ptr_cb + uv_dct_offset, wrap_c);
                s->dsp.get_pixels(s->block[9], ptr_cr + uv_dct_offset, wrap_c);
                s->dsp.get_pixels(s->block[10], ptr_cb + uv_dct_offset + 8, wrap_c);
                s->dsp.get_pixels(s->block[11], ptr_cr + uv_dct_offset + 8, wrap_c);
            }
        }
//        LOGD("encode_mb_internal BA pbc:%d\n",put_bits_count(&(s->pb)));
    } else {
//        LOGD("encode_mb_internal BB pbc:%d\n",put_bits_count(&(s->pb)));
        op_pixels_func (*op_pix)[4];
        qpel_mc_func (*op_qpix)[16];
        uint8_t *dest_y, *dest_cb, *dest_cr;

        dest_y  = s->dest[0];
        dest_cb = s->dest[1];
        dest_cr = s->dest[2];

    	if ((!s->no_rounding) || s->pict_type == AV_PICTURE_TYPE_B) {
            op_pix  = s->dsp.put_pixels_tab;
            op_qpix = s->dsp.put_qpel_pixels_tab;
        } else {
            op_pix  = s->dsp.put_no_rnd_pixels_tab;
            op_qpix = s->dsp.put_no_rnd_qpel_pixels_tab;
        }

        if (s->mv_dir & MV_DIR_FORWARD) {
            ff_MPV_motion(s, dest_y, dest_cb, dest_cr, 0,
                          s->last_picture.f.data,
                          op_pix, op_qpix);
            op_pix  = s->dsp.avg_pixels_tab;
            op_qpix = s->dsp.avg_qpel_pixels_tab;
        }
        if (s->mv_dir & MV_DIR_BACKWARD) {
            ff_MPV_motion(s, dest_y, dest_cb, dest_cr, 1,
                          s->next_picture.f.data,
                          op_pix, op_qpix);
        }

        if (s->flags & CODEC_FLAG_INTERLACED_DCT) {
            int progressive_score, interlaced_score;

            s->interlaced_dct = 0;
            progressive_score = s->dsp.ildct_cmp[0](s, dest_y,
                                                    ptr_y,              wrap_y,
                                                    8) +
                                s->dsp.ildct_cmp[0](s, dest_y + wrap_y * 8,
                                                    ptr_y + wrap_y * 8, wrap_y,
                                                    8) - 400;

            if (s->avctx->ildct_cmp == FF_CMP_VSSE)
                progressive_score -= 400;

            if (progressive_score > 0) {
                interlaced_score = s->dsp.ildct_cmp[0](s, dest_y,
                                                       ptr_y,
                                                       wrap_y * 2, 8) +
                                   s->dsp.ildct_cmp[0](s, dest_y + wrap_y,
                                                       ptr_y + wrap_y,
                                                       wrap_y * 2, 8);

                if (progressive_score > interlaced_score) {
                    s->interlaced_dct = 1;

                    dct_offset = wrap_y;
                    uv_dct_offset = wrap_c;
                    wrap_y <<= 1;
                    if (s->chroma_format == CHROMA_422)
                        wrap_c <<= 1;
                }
            }
        }

        s->dsp.diff_pixels(s->block[0], ptr_y, dest_y, wrap_y);
        s->dsp.diff_pixels(s->block[1], ptr_y + 8, dest_y + 8, wrap_y);
        s->dsp.diff_pixels(s->block[2], ptr_y + dct_offset,
                           dest_y + dct_offset, wrap_y);
        s->dsp.diff_pixels(s->block[3], ptr_y + dct_offset + 8,
                           dest_y + dct_offset + 8, wrap_y);

        if (s->flags & CODEC_FLAG_GRAY) {
            skip_dct[4] = 1;
            skip_dct[5] = 1;
        } else {
            s->dsp.diff_pixels(s->block[4], ptr_cb, dest_cb, wrap_c);
            s->dsp.diff_pixels(s->block[5], ptr_cr, dest_cr, wrap_c);
            if (!s->chroma_y_shift) { /* 422 */
                s->dsp.diff_pixels(s->block[6], ptr_cb + uv_dct_offset,
                                   dest_cb + uv_dct_offset, wrap_c);
                s->dsp.diff_pixels(s->block[7], ptr_cr + uv_dct_offset,
                                   dest_cr + uv_dct_offset, wrap_c);
            }
        }
        /* pre quantization */
        if (s->current_picture.mc_mb_var[s->mb_stride * mb_y + mb_x] <
                2 * s->qscale * s->qscale) {
            // FIXME optimize
            if (s->dsp.sad[1](NULL, ptr_y , dest_y,
                              wrap_y, 8) < 20 * s->qscale)
                skip_dct[0] = 1;
            if (s->dsp.sad[1](NULL, ptr_y + 8,
                              dest_y + 8, wrap_y, 8) < 20 * s->qscale)
                skip_dct[1] = 1;
            if (s->dsp.sad[1](NULL, ptr_y + dct_offset,
                              dest_y + dct_offset, wrap_y, 8) < 20 * s->qscale)
                skip_dct[2] = 1;
            if (s->dsp.sad[1](NULL, ptr_y + dct_offset + 8,
                              dest_y + dct_offset + 8,
                              wrap_y, 8) < 20 * s->qscale)
                skip_dct[3] = 1;
            if (s->dsp.sad[1](NULL, ptr_cb, dest_cb,
                              wrap_c, 8) < 20 * s->qscale)
                skip_dct[4] = 1;
            if (s->dsp.sad[1](NULL, ptr_cr, dest_cr,
                              wrap_c, 8) < 20 * s->qscale)
                skip_dct[5] = 1;
            if (!s->chroma_y_shift) { /* 422 */
                if (s->dsp.sad[1](NULL, ptr_cb + uv_dct_offset,
                                  dest_cb + uv_dct_offset,
                                  wrap_c, 8) < 20 * s->qscale)
                    skip_dct[6] = 1;
                if (s->dsp.sad[1](NULL, ptr_cr + uv_dct_offset,
                                  dest_cr + uv_dct_offset,
                                  wrap_c, 8) < 20 * s->qscale)
                    skip_dct[7] = 1;
            }
        }
//    	LOGD("Tblock_last_index[%d]:%d\n",i,s->block_last_index[i]);
    }//End of s->mb_intra else

    if (s->quantizer_noise_shaping) {
        if (!skip_dct[0])
            get_visual_weight(weight[0], ptr_y                 , wrap_y);
        if (!skip_dct[1])
            get_visual_weight(weight[1], ptr_y              + 8, wrap_y);
        if (!skip_dct[2])
            get_visual_weight(weight[2], ptr_y + dct_offset    , wrap_y);
        if (!skip_dct[3])
            get_visual_weight(weight[3], ptr_y + dct_offset + 8, wrap_y);
        if (!skip_dct[4])
            get_visual_weight(weight[4], ptr_cb                , wrap_c);
        if (!skip_dct[5])
            get_visual_weight(weight[5], ptr_cr                , wrap_c);
        if (!s->chroma_y_shift) { /* 422 */
            if (!skip_dct[6])
                get_visual_weight(weight[6], ptr_cb + uv_dct_offset,
                                  wrap_c);
            if (!skip_dct[7])
                get_visual_weight(weight[7], ptr_cr + uv_dct_offset,
                                  wrap_c);
        }
        memcpy(orig[0], s->block[0], sizeof(int16_t) * 64 * mb_block_count);
    }

//  	 int index;

    /* DCT & quantize */
//    av_assert2(s->out_format != FMT_MJPEG || s->qscale == 8);
    {

//		LOGD("\n");
//
//       for(index = 10; index < 10; index++){
//       	LOGD("Zblock_last_index[%d]:%d\n",index,s->block_last_index[index]);
//       }
        for (i = 0; i < mb_block_count; i++) {
            if (!skip_dct[i]) {
                int overflow;
                s->block_last_index[i] = s->dct_quantize(s, s->block[i], i, s->qscale, &overflow);
                // FIXME we could decide to change to quantizer instead of
                // clipping
                // JS: I don't think that would be a good idea it could lower
                //     quality instead of improve it. Just INTRADC clipping
                //     deserves changes in quantizer
                if (overflow)
                    clip_coeffs(s, s->block[i], s->block_last_index[i]);
            } else
                s->block_last_index[i] = -1;
//            	LOGD("Tblock_last_index[%d]:%d\n",i,s->block_last_index[i]);
        }


        if (s->quantizer_noise_shaping) {
            for (i = 0; i < mb_block_count; i++) {
                if (!skip_dct[i]) {
                    s->block_last_index[i] =
                        dct_quantize_refine(s, s->block[i], weight[i],
                                            orig[i], i, s->qscale);
                }
            }
        }

        if (s->luma_elim_threshold && !s->mb_intra)
            for (i = 0; i < 4; i++)
                dct_single_coeff_elimination(s, i, s->luma_elim_threshold);
        if (s->chroma_elim_threshold && !s->mb_intra)
            for (i = 4; i < mb_block_count; i++)
                dct_single_coeff_elimination(s, i, s->chroma_elim_threshold);

        if (s->mpv_flags & FF_MPV_FLAG_CBP_RD) {
            for (i = 0; i < mb_block_count; i++) {
                if (s->block_last_index[i] == -1)
                    s->coded_score[i] = INT_MAX / 256;
            }
        }
    }

//    LOGD("encode_mb_internal C pbc:%d\n",put_bits_count(&(s->pb)));
    if ((s->flags & CODEC_FLAG_GRAY) && s->mb_intra) {
        s->block_last_index[4] =
        s->block_last_index[5] = 0;
        s->block[4][0] =
        s->block[5][0] = (1024 + s->c_dc_scale / 2) / s->c_dc_scale;
        if (!s->chroma_y_shift) { /* 422 / 444 */
            for (i=6; i<12; i++) {
                s->block_last_index[i] = 0;
                s->block[i][0] = s->block[4][0];
            }
        }
    }

    // non c quantize code returns incorrect block_last_index FIXME
    if (s->alternate_scan && s->dct_quantize != ff_dct_quantize_c) {
        for (i = 0; i < mb_block_count; i++) {
            int j;
            if (s->block_last_index[i] > 0) {
                for (j = 63; j > 0; j--) {
                    if (s->block[i][s->intra_scantable.permutated[j]])
                        break;
                }
                s->block_last_index[i] = j;
            }
        }
    }

    ff_h263_encode_mb(s, s->block, motion_x, motion_y);

}
static inline void encode_mb(MpegEncContext *s, int motion_x, int motion_y)
{
    if (s->chroma_format == CHROMA_420) encode_mb_internal(s, motion_x, motion_y,  8, 8, 6);
    else if (s->chroma_format == CHROMA_422) encode_mb_internal(s, motion_x, motion_y, 16, 8, 8);
    else encode_mb_internal(s, motion_x, motion_y, 16, 16, 12);
}


/**
 * Clean dc, ac, coded_block for the current non-intra MB.
 */
void ff_clean_intra_table_entries(MpegEncContext *s)
{
    int wrap = s->b8_stride;
    int xy = s->block_index[0];

    s->dc_val[0][xy           ] =
    s->dc_val[0][xy + 1       ] =
    s->dc_val[0][xy     + wrap] =
    s->dc_val[0][xy + 1 + wrap] = 1024;
    /* ac pred */
    memset(s->ac_val[0][xy       ], 0, 32 * sizeof(int16_t));
    memset(s->ac_val[0][xy + wrap], 0, 32 * sizeof(int16_t));
    if (s->msmpeg4_version>=3) {
        s->coded_block[xy           ] =
        s->coded_block[xy + 1       ] =
        s->coded_block[xy     + wrap] =
        s->coded_block[xy + 1 + wrap] = 0;
    }
    /* chroma */
    wrap = s->mb_stride;
    xy = s->mb_x + s->mb_y * wrap;
    s->dc_val[1][xy] =
    s->dc_val[2][xy] = 1024;
    /* ac pred */
    memset(s->ac_val[1][xy], 0, 16 * sizeof(int16_t));
    memset(s->ac_val[2][xy], 0, 16 * sizeof(int16_t));

    s->mbintra_table[xy]= 0;
}


void ff_thread_await_progress(AVFrame *f, int n, int field)
{
    PerThreadContext *p;
    volatile int *progress = f->thread_opaque;

    if (!progress || progress[field] >= n) return;

    p = f->owner->thread_opaque;

    if (f->owner->debug&FF_DEBUG_THREADS)
        printf( "thread awaiting %d field %d from %p\n", n, field, progress);

    pthread_mutex_lock(&p->progress_mutex);
    while (progress[field] < n)
        pthread_cond_wait(&p->progress_cond, &p->progress_mutex);
    pthread_mutex_unlock(&p->progress_mutex);
}


static inline void add_dequant_dct(MpegEncContext *s,
                           int16_t *block, int i, uint8_t *dest, int line_size, int qscale)
{
    if (s->block_last_index[i] >= 0) {
        s->dct_unquantize_inter(s, block, i, qscale);

        s->dsp.idct_add (dest, line_size, block);
    }
}


/**
 * find the lowest MB row referenced in the MVs
 */
int ff_MPV_lowest_referenced_row(MpegEncContext *s, int dir)
{
    int my_max = INT_MIN, my_min = INT_MAX, qpel_shift = !s->quarter_sample;
    int my, off, i, mvs;

    if (s->picture_structure != PICT_FRAME || s->mcsel)
        goto unhandled;

    switch (s->mv_type) {
        case MV_TYPE_16X16:
            mvs = 1;
            break;
        case MV_TYPE_16X8:
            mvs = 2;
            break;
        case MV_TYPE_8X8:
            mvs = 4;
            break;
        default:
            goto unhandled;
    }

    for (i = 0; i < mvs; i++) {
        my = s->mv[dir][i][1]<<qpel_shift;
        my_max = FFMAX(my_max, my);
        my_min = FFMIN(my_min, my);
    }

    off = (FFMAX(-my_min, my_max) + 63) >> 6;

    return FFMIN(FFMAX(s->mb_y + off, 0), s->mb_height-1);
unhandled:
    return s->mb_height-1;
}


/* apply one mpeg motion vector to the three components */
static inline void mpeg_motion_lowres(MpegEncContext *s,
                                                uint8_t *dest_y,
                                                uint8_t *dest_cb,
                                                uint8_t *dest_cr,
                                                int field_based,
                                                int bottom_field,
                                                int field_select,
                                                uint8_t **ref_picture,
                                                h264_chroma_mc_func *pix_op,
                                                int motion_x, int motion_y,
                                                int h, int mb_y)
{
    uint8_t *ptr_y, *ptr_cb, *ptr_cr;
    int mx, my, src_x, src_y, uvsrc_x, uvsrc_y, uvlinesize, linesize, sx, sy,
        uvsx, uvsy;
    const int lowres     = s->avctx->lowres;
    const int op_index   = FFMIN(lowres-1+s->chroma_x_shift, 2);
    const int block_s    = 8>>lowres;
    const int s_mask     = (2 << lowres) - 1;
    const int h_edge_pos = s->h_edge_pos >> lowres;
    const int v_edge_pos = s->v_edge_pos >> lowres;
    linesize   = s->current_picture.f.linesize[0] << field_based;
    uvlinesize = s->current_picture.f.linesize[1] << field_based;

    // FIXME obviously not perfect but qpel will not work in lowres anyway
    if (s->quarter_sample) {
        motion_x /= 2;
        motion_y /= 2;
    }

    if(field_based){
        motion_y += (bottom_field - field_select)*((1 << lowres)-1);
    }

    sx = motion_x & s_mask;
    sy = motion_y & s_mask;
    src_x = s->mb_x * 2 * block_s + (motion_x >> lowres + 1);
    src_y = (mb_y * 2 * block_s >> field_based) + (motion_y >> lowres + 1);

    if (s->out_format == FMT_H263) {
        uvsx    = ((motion_x >> 1) & s_mask) | (sx & 1);
        uvsy    = ((motion_y >> 1) & s_mask) | (sy & 1);
        uvsrc_x = src_x >> 1;
        uvsrc_y = src_y >> 1;
    } else if (s->out_format == FMT_H261) {
        // even chroma mv's are full pel in H261
        mx      = motion_x / 4;
        my      = motion_y / 4;
        uvsx    = (2 * mx) & s_mask;
        uvsy    = (2 * my) & s_mask;
        uvsrc_x = s->mb_x * block_s + (mx >> lowres);
        uvsrc_y =    mb_y * block_s + (my >> lowres);
    } else {
        if(s->chroma_y_shift){
            mx      = motion_x / 2;
            my      = motion_y / 2;
            uvsx    = mx & s_mask;
            uvsy    = my & s_mask;
            uvsrc_x = s->mb_x * block_s                 + (mx >> lowres + 1);
            uvsrc_y =   (mb_y * block_s >> field_based) + (my >> lowres + 1);
        } else {
            if(s->chroma_x_shift){
            //Chroma422
                mx = motion_x / 2;
                uvsx = mx & s_mask;
                uvsy = motion_y & s_mask;
                uvsrc_y = src_y;
                uvsrc_x = s->mb_x*block_s               + (mx >> (lowres+1));
            } else {
            //Chroma444
                uvsx = motion_x & s_mask;
                uvsy = motion_y & s_mask;
                uvsrc_x = src_x;
                uvsrc_y = src_y;
            }
        }
    }

    ptr_y  = ref_picture[0] + src_y   * linesize   + src_x;
    ptr_cb = ref_picture[1] + uvsrc_y * uvlinesize + uvsrc_x;
    ptr_cr = ref_picture[2] + uvsrc_y * uvlinesize + uvsrc_x;

    if ((unsigned) src_x > FFMAX( h_edge_pos - (!!sx) - 2 * block_s,       0) ||
        (unsigned) src_y > FFMAX((v_edge_pos >> field_based) - (!!sy) - h, 0)) {
        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr_y,
                                linesize >> field_based, 17, 17 + field_based,
                                src_x, src_y << field_based, h_edge_pos,
                                v_edge_pos);
        ptr_y = s->edge_emu_buffer;
        if (!CONFIG_GRAY || !(s->flags & CODEC_FLAG_GRAY)) {
            uint8_t *uvbuf = s->edge_emu_buffer + 18 * s->linesize;
            s->vdsp.emulated_edge_mc(uvbuf , ptr_cb, uvlinesize >> field_based, 9,
                                    9 + field_based,
                                    uvsrc_x, uvsrc_y << field_based,
                                    h_edge_pos >> 1, v_edge_pos >> 1);
            s->vdsp.emulated_edge_mc(uvbuf + 16, ptr_cr, uvlinesize >> field_based, 9,
                                    9 + field_based,
                                    uvsrc_x, uvsrc_y << field_based,
                                    h_edge_pos >> 1, v_edge_pos >> 1);
            ptr_cb = uvbuf;
            ptr_cr = uvbuf + 16;
        }
    }

    // FIXME use this for field pix too instead of the obnoxious hack which changes picture.f.data
    if (bottom_field) {
        dest_y  += s->linesize;
        dest_cb += s->uvlinesize;
        dest_cr += s->uvlinesize;
    }

    if (field_select) {
        ptr_y   += s->linesize;
        ptr_cb  += s->uvlinesize;
        ptr_cr  += s->uvlinesize;
    }

    sx = (sx << 2) >> lowres;
    sy = (sy << 2) >> lowres;
    pix_op[lowres - 1](dest_y, ptr_y, linesize, h, sx, sy);

    if (!CONFIG_GRAY || !(s->flags & CODEC_FLAG_GRAY)) {
        uvsx = (uvsx << 2) >> lowres;
        uvsy = (uvsy << 2) >> lowres;
        if (h >> s->chroma_y_shift) {
            pix_op[op_index](dest_cb, ptr_cb, uvlinesize, h >> s->chroma_y_shift, uvsx, uvsy);
            pix_op[op_index](dest_cr, ptr_cr, uvlinesize, h >> s->chroma_y_shift, uvsx, uvsy);
        }
    }
    // FIXME h261 lowres loop filter
}


static inline int hpel_motion_lowres(MpegEncContext *s,
                                     uint8_t *dest, uint8_t *src,
                                     int field_based, int field_select,
                                     int src_x, int src_y,
                                     int width, int height, int stride,
                                     int h_edge_pos, int v_edge_pos,
                                     int w, int h, h264_chroma_mc_func *pix_op,
                                     int motion_x, int motion_y)
{
    const int lowres   = s->avctx->lowres;
    const int op_index = FFMIN(lowres, 2);
    const int s_mask   = (2 << lowres) - 1;
    int emu = 0;
    int sx, sy;

    if (s->quarter_sample) {
        motion_x /= 2;
        motion_y /= 2;
    }

    sx = motion_x & s_mask;
    sy = motion_y & s_mask;
    src_x += motion_x >> lowres + 1;
    src_y += motion_y >> lowres + 1;

    src   += src_y * stride + src_x;

    if ((unsigned)src_x > FFMAX( h_edge_pos - (!!sx) - w,                 0) ||
        (unsigned)src_y > FFMAX((v_edge_pos >> field_based) - (!!sy) - h, 0)) {
        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, src, s->linesize, w + 1,
                                (h + 1) << field_based, src_x,
                                src_y   << field_based,
                                h_edge_pos,
                                v_edge_pos);
        src = s->edge_emu_buffer;
        emu = 1;
    }

    sx = (sx << 2) >> lowres;
    sy = (sy << 2) >> lowres;
    if (field_select)
        src += s->linesize;
    pix_op[op_index](dest, src, stride, h, sx, sy);
    return emu;
}

static inline void chroma_4mv_motion_lowres(MpegEncContext *s,
                                            uint8_t *dest_cb, uint8_t *dest_cr,
                                            uint8_t **ref_picture,
                                            h264_chroma_mc_func * pix_op,
                                            int mx, int my)
{
    const int lowres     = s->avctx->lowres;
    const int op_index   = FFMIN(lowres, 2);
    const int block_s    = 8 >> lowres;
    const int s_mask     = (2 << lowres) - 1;
    const int h_edge_pos = s->h_edge_pos >> lowres + 1;
    const int v_edge_pos = s->v_edge_pos >> lowres + 1;
    int emu = 0, src_x, src_y, offset, sx, sy;
    uint8_t *ptr;

    if (s->quarter_sample) {
        mx /= 2;
        my /= 2;
    }

    /* In case of 8X8, we construct a single chroma motion vector
       with a special rounding */
    mx = ff_h263_round_chroma(mx);
    my = ff_h263_round_chroma(my);

    sx = mx & s_mask;
    sy = my & s_mask;
    src_x = s->mb_x * block_s + (mx >> lowres + 1);
    src_y = s->mb_y * block_s + (my >> lowres + 1);

    offset = src_y * s->uvlinesize + src_x;
    ptr = ref_picture[1] + offset;
    if (s->flags & CODEC_FLAG_EMU_EDGE) {
        if ((unsigned) src_x > FFMAX(h_edge_pos - (!!sx) - block_s, 0) ||
            (unsigned) src_y > FFMAX(v_edge_pos - (!!sy) - block_s, 0)) {
            s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, s->uvlinesize,
                                    9, 9, src_x, src_y, h_edge_pos, v_edge_pos);
            ptr = s->edge_emu_buffer;
            emu = 1;
        }
    }
    sx = (sx << 2) >> lowres;
    sy = (sy << 2) >> lowres;
    pix_op[op_index](dest_cb, ptr, s->uvlinesize, block_s, sx, sy);

    ptr = ref_picture[2] + offset;
    if (emu) {
        s->vdsp.emulated_edge_mc(s->edge_emu_buffer, ptr, s->uvlinesize, 9, 9,
                                src_x, src_y, h_edge_pos, v_edge_pos);
        ptr = s->edge_emu_buffer;
    }
    pix_op[op_index](dest_cr, ptr, s->uvlinesize, block_s, sx, sy);
}

/**
 * motion compensation of a single macroblock
 * @param s context
 * @param dest_y luma destination pointer
 * @param dest_cb chroma cb/u destination pointer
 * @param dest_cr chroma cr/v destination pointer
 * @param dir direction (0->forward, 1->backward)
 * @param ref_picture array[3] of pointers to the 3 planes of the reference picture
 * @param pix_op halfpel motion compensation function (average or put normally)
 * the motion vectors are taken from s->mv and the MV type from s->mv_type
 */
static inline void MPV_motion_lowres(MpegEncContext *s,
                                     uint8_t *dest_y, uint8_t *dest_cb,
                                     uint8_t *dest_cr,
                                     int dir, uint8_t **ref_picture,
                                     h264_chroma_mc_func *pix_op)
{
    int mx, my;
    int mb_x, mb_y, i;
    const int lowres  = s->avctx->lowres;
    const int block_s = 8 >>lowres;

    mb_x = s->mb_x;
    mb_y = s->mb_y;

    switch (s->mv_type) {
    case MV_TYPE_16X16:
        mpeg_motion_lowres(s, dest_y, dest_cb, dest_cr,
                           0, 0, 0,
                           ref_picture, pix_op,
                           s->mv[dir][0][0], s->mv[dir][0][1],
                           2 * block_s, mb_y);
        break;
    case MV_TYPE_8X8:
        mx = 0;
        my = 0;
        for (i = 0; i < 4; i++) {
            hpel_motion_lowres(s, dest_y + ((i & 1) + (i >> 1) *
                               s->linesize) * block_s,
                               ref_picture[0], 0, 0,
                               (2 * mb_x + (i & 1)) * block_s,
                               (2 * mb_y + (i >> 1)) * block_s,
                               s->width, s->height, s->linesize,
                               s->h_edge_pos >> lowres, s->v_edge_pos >> lowres,
                               block_s, block_s, pix_op,
                               s->mv[dir][i][0], s->mv[dir][i][1]);

            mx += s->mv[dir][i][0];
            my += s->mv[dir][i][1];
        }

        if (!CONFIG_GRAY || !(s->flags & CODEC_FLAG_GRAY))
            chroma_4mv_motion_lowres(s, dest_cb, dest_cr, ref_picture,
                                     pix_op, mx, my);
        break;
    case MV_TYPE_FIELD:
        if (s->picture_structure == PICT_FRAME) {
            /* top field */
            mpeg_motion_lowres(s, dest_y, dest_cb, dest_cr,
                               1, 0, s->field_select[dir][0],
                               ref_picture, pix_op,
                               s->mv[dir][0][0], s->mv[dir][0][1],
                               block_s, mb_y);
            /* bottom field */
            mpeg_motion_lowres(s, dest_y, dest_cb, dest_cr,
                               1, 1, s->field_select[dir][1],
                               ref_picture, pix_op,
                               s->mv[dir][1][0], s->mv[dir][1][1],
                               block_s, mb_y);
        } else {
            if (s->picture_structure != s->field_select[dir][0] + 1 &&
                s->pict_type != AV_PICTURE_TYPE_B && !s->first_field) {
                ref_picture = s->current_picture_ptr->f.data;

            }
            mpeg_motion_lowres(s, dest_y, dest_cb, dest_cr,
                               0, 0, s->field_select[dir][0],
                               ref_picture, pix_op,
                               s->mv[dir][0][0],
                               s->mv[dir][0][1], 2 * block_s, mb_y >> 1);
            }
        break;
    case MV_TYPE_16X8:
        for (i = 0; i < 2; i++) {
            uint8_t **ref2picture;

            if (s->picture_structure == s->field_select[dir][i] + 1 ||
                s->pict_type == AV_PICTURE_TYPE_B || s->first_field) {
                ref2picture = ref_picture;
            } else {
                ref2picture = s->current_picture_ptr->f.data;
            }

            mpeg_motion_lowres(s, dest_y, dest_cb, dest_cr,
                               0, 0, s->field_select[dir][i],
                               ref2picture, pix_op,
                               s->mv[dir][i][0], s->mv[dir][i][1] +
                               2 * block_s * i, block_s, mb_y >> 1);

            dest_y  +=  2 * block_s *  s->linesize;
            dest_cb += (2 * block_s >> s->chroma_y_shift) * s->uvlinesize;
            dest_cr += (2 * block_s >> s->chroma_y_shift) * s->uvlinesize;
        }
        break;
    case MV_TYPE_DMV:
        if (s->picture_structure == PICT_FRAME) {
            for (i = 0; i < 2; i++) {
                int j;
                for (j = 0; j < 2; j++) {
                    mpeg_motion_lowres(s, dest_y, dest_cb, dest_cr,
                                       1, j, j ^ i,
                                       ref_picture, pix_op,
                                       s->mv[dir][2 * i + j][0],
                                       s->mv[dir][2 * i + j][1],
                                       block_s, mb_y);
                }
                pix_op = s->h264chroma.avg_h264_chroma_pixels_tab;
            }
        } else {
            for (i = 0; i < 2; i++) {
                mpeg_motion_lowres(s, dest_y, dest_cb, dest_cr,
                                   0, 0, s->picture_structure != i + 1,
                                   ref_picture, pix_op,
                                   s->mv[dir][2 * i][0],s->mv[dir][2 * i][1],
                                   2 * block_s, mb_y >> 1);

                // after put we make avg of the same block
                pix_op = s->h264chroma.avg_h264_chroma_pixels_tab;

                // opposite parity is always in the same
                // frame if this is second field
                if (!s->first_field) {
                    ref_picture = s->current_picture_ptr->f.data;
                }
            }
        }
        break;
//    default:
//        av_assert2(0);
    }
}

/* put block[] to dest[] */
static inline void put_dct(MpegEncContext *s,
                           int16_t *block, int i, uint8_t *dest, int line_size, int qscale)
{
    s->dct_unquantize_intra(s, block, i, qscale);
    s->dsp.idct_put (dest, line_size, block);
}

/* add block[] to dest[] */
static inline void add_dct(MpegEncContext *s,
                           int16_t *block, int i, uint8_t *dest, int line_size)
{
    if (s->block_last_index[i] >= 0) {
        s->dsp.idct_add (dest, line_size, block);
    }
}

/* generic function called after a macroblock has been parsed by the
   decoder or after it has been encoded by the encoder.

   Important variables used:
   s->mb_intra : true if intra macroblock
   s->mv_dir   : motion vector direction
   s->mv_type  : motion vector type
   s->mv       : motion vector
   s->interlaced_dct : true if interlaced dct used (mpeg2)
 */
static inline void MPV_decode_mb_internal(MpegEncContext *s, int16_t block[12][64],
                            int lowres_flag, int is_mpeg12)
{
    const int mb_xy = s->mb_y * s->mb_stride + s->mb_x;
    if(CONFIG_MPEG_XVMC_DECODER && s->avctx->xvmc_acceleration){
        ff_xvmc_decode_mb(s);//xvmc uses pblocks
        return;
    }

    if(s->avctx->debug&FF_DEBUG_DCT_COEFF) {
       /* save DCT coefficients */
       int i,j;
       int16_t *dct = &s->current_picture.f.dct_coeff[mb_xy * 64 * 6];
       printf( "DCT coeffs of MB at %dx%d:\n", s->mb_x, s->mb_y);
       for(i=0; i<6; i++){
           for(j=0; j<64; j++){
               *dct++ = block[i][s->dsp.idct_permutation[j]];
               printf( "%5d", dct[-1]);
           }
           printf( "\n");
       }
    }

    s->current_picture.f.qscale_table[mb_xy] = s->qscale;

    /* update DC predictors for P macroblocks */
    if (!s->mb_intra) {
        if (!is_mpeg12 && (s->h263_pred || s->h263_aic)) {
            if(s->mbintra_table[mb_xy])
                ff_clean_intra_table_entries(s);
        } else {
            s->last_dc[0] =
            s->last_dc[1] =
            s->last_dc[2] = 128 << s->intra_dc_precision;
        }
    }
    else if (!is_mpeg12 && (s->h263_pred || s->h263_aic))
        s->mbintra_table[mb_xy]=1;

    if ((s->flags&CODEC_FLAG_PSNR) || !(s->encoding && (s->intra_only || s->pict_type==AV_PICTURE_TYPE_B) && s->avctx->mb_decision != FF_MB_DECISION_RD)) { //FIXME precalc
        uint8_t *dest_y, *dest_cb, *dest_cr;
        int dct_linesize, dct_offset;
        op_pixels_func (*op_pix)[4];
        qpel_mc_func (*op_qpix)[16];
        const int linesize   = s->current_picture.f.linesize[0]; //not s->linesize as this would be wrong for field pics
        const int uvlinesize = s->current_picture.f.linesize[1];
        const int readable= s->pict_type != AV_PICTURE_TYPE_B || s->encoding || s->avctx->draw_horiz_band || lowres_flag;
        const int block_size= lowres_flag ? 8>>s->avctx->lowres : 8;

        /* avoid copy if macroblock skipped in last frame too */
        /* skip only during decoding as we might trash the buffers during encoding a bit */
        if(!s->encoding){
            uint8_t *mbskip_ptr = &s->mbskip_table[mb_xy];

            if (s->mb_skipped) {
                s->mb_skipped= 0;
//                av_assert2(s->pict_type!=AV_PICTURE_TYPE_I);
                *mbskip_ptr = 1;
            } else if(!s->current_picture.f.reference) {
                *mbskip_ptr = 1;
            } else{
                *mbskip_ptr = 0; /* not skipped */
            }
        }

        dct_linesize = linesize << s->interlaced_dct;
        dct_offset   = s->interlaced_dct ? linesize : linesize * block_size;

        if(readable){
            dest_y=  s->dest[0];
            dest_cb= s->dest[1];
            dest_cr= s->dest[2];
        }else{
            dest_y = s->b_scratchpad;
            dest_cb= s->b_scratchpad+16*linesize;
            dest_cr= s->b_scratchpad+32*linesize;
        }

        if (!s->mb_intra) {
            /* motion handling */
            /* decoding or more than one mb_type (MC was already done otherwise) */
            if(!s->encoding){

                if(HAVE_THREADS && (s->avctx->active_thread_type&FF_THREAD_FRAME)) {
                    if (s->mv_dir & MV_DIR_FORWARD) {
                        ff_thread_await_progress(&s->last_picture_ptr->f,
                                                 ff_MPV_lowest_referenced_row(s, 0),
                                                 0);
                    }
                    if (s->mv_dir & MV_DIR_BACKWARD) {
                        ff_thread_await_progress(&s->next_picture_ptr->f,
                                                 ff_MPV_lowest_referenced_row(s, 1),
                                                 0);
                    }
                }

                if(lowres_flag){
                    h264_chroma_mc_func *op_pix = s->h264chroma.put_h264_chroma_pixels_tab;

                    if (s->mv_dir & MV_DIR_FORWARD) {
                        MPV_motion_lowres(s, dest_y, dest_cb, dest_cr, 0, s->last_picture.f.data, op_pix);
                        op_pix = s->h264chroma.avg_h264_chroma_pixels_tab;
                    }
                    if (s->mv_dir & MV_DIR_BACKWARD) {
                        MPV_motion_lowres(s, dest_y, dest_cb, dest_cr, 1, s->next_picture.f.data, op_pix);
                    }
                }else{
                    op_qpix= s->me.qpel_put;
                    if ((!s->no_rounding) || s->pict_type==AV_PICTURE_TYPE_B){
                        op_pix = s->dsp.put_pixels_tab;
                    }else{
                        op_pix = s->dsp.put_no_rnd_pixels_tab;
                    }
                    if (s->mv_dir & MV_DIR_FORWARD) {
                        ff_MPV_motion(s, dest_y, dest_cb, dest_cr, 0, s->last_picture.f.data, op_pix, op_qpix);
                        op_pix = s->dsp.avg_pixels_tab;
                        op_qpix= s->me.qpel_avg;
                    }
                    if (s->mv_dir & MV_DIR_BACKWARD) {
                        ff_MPV_motion(s, dest_y, dest_cb, dest_cr, 1, s->next_picture.f.data, op_pix, op_qpix);
                    }
                }
            }

            /* skip dequant / idct if we are really late ;) */
            if(s->avctx->skip_idct){
                if(  (s->avctx->skip_idct >= AVDISCARD_NONREF && s->pict_type == AV_PICTURE_TYPE_B)
                   ||(s->avctx->skip_idct >= AVDISCARD_NONKEY && s->pict_type != AV_PICTURE_TYPE_I)
                   || s->avctx->skip_idct >= AVDISCARD_ALL)
                    goto skip_idct;
            }

            /* add dct residue */
            if(s->encoding || !(   s->msmpeg4_version || s->codec_id==AV_CODEC_ID_MPEG1VIDEO || s->codec_id==AV_CODEC_ID_MPEG2VIDEO
                                || (s->codec_id==AV_CODEC_ID_MPEG4 && !s->mpeg_quant))){
                add_dequant_dct(s, block[0], 0, dest_y                          , dct_linesize, s->qscale);
                add_dequant_dct(s, block[1], 1, dest_y              + block_size, dct_linesize, s->qscale);
                add_dequant_dct(s, block[2], 2, dest_y + dct_offset             , dct_linesize, s->qscale);
                add_dequant_dct(s, block[3], 3, dest_y + dct_offset + block_size, dct_linesize, s->qscale);

                if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY)){
                    if (s->chroma_y_shift){
                        add_dequant_dct(s, block[4], 4, dest_cb, uvlinesize, s->chroma_qscale);
                        add_dequant_dct(s, block[5], 5, dest_cr, uvlinesize, s->chroma_qscale);
                    }else{
                        dct_linesize >>= 1;
                        dct_offset >>=1;
                        add_dequant_dct(s, block[4], 4, dest_cb,              dct_linesize, s->chroma_qscale);
                        add_dequant_dct(s, block[5], 5, dest_cr,              dct_linesize, s->chroma_qscale);
                        add_dequant_dct(s, block[6], 6, dest_cb + dct_offset, dct_linesize, s->chroma_qscale);
                        add_dequant_dct(s, block[7], 7, dest_cr + dct_offset, dct_linesize, s->chroma_qscale);
                    }
                }
            } else if(is_mpeg12 || (s->codec_id != AV_CODEC_ID_WMV2)){
            	//dct系は省略
                add_dct(s, block[0], 0, dest_y                          , dct_linesize);
                add_dct(s, block[1], 1, dest_y              + block_size, dct_linesize);
                add_dct(s, block[2], 2, dest_y + dct_offset             , dct_linesize);
                add_dct(s, block[3], 3, dest_y + dct_offset + block_size, dct_linesize);

                if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY)){
                    if(s->chroma_y_shift){//Chroma420
                        add_dct(s, block[4], 4, dest_cb, uvlinesize);
                        add_dct(s, block[5], 5, dest_cr, uvlinesize);
                    }else{
                        //chroma422
                        dct_linesize = uvlinesize << s->interlaced_dct;
                        dct_offset   = s->interlaced_dct ? uvlinesize : uvlinesize*block_size;

                        add_dct(s, block[4], 4, dest_cb, dct_linesize);
                        add_dct(s, block[5], 5, dest_cr, dct_linesize);
                        add_dct(s, block[6], 6, dest_cb+dct_offset, dct_linesize);
                        add_dct(s, block[7], 7, dest_cr+dct_offset, dct_linesize);
                        if(!s->chroma_x_shift){//Chroma444
                            add_dct(s, block[8], 8, dest_cb+block_size, dct_linesize);
                            add_dct(s, block[9], 9, dest_cr+block_size, dct_linesize);
                            add_dct(s, block[10], 10, dest_cb+block_size+dct_offset, dct_linesize);
                            add_dct(s, block[11], 11, dest_cr+block_size+dct_offset, dct_linesize);
                        }
                    }
                }//fi gray
            }
            else if (CONFIG_WMV2_DECODER || CONFIG_WMV2_ENCODER) {
            		//明らかにWMVなので省略
//                ff_wmv2_add_mb(s, block, dest_y, dest_cb, dest_cr);
            }
        } else {
            /* dct only in intra block */
            if(s->encoding || !(s->codec_id==AV_CODEC_ID_MPEG1VIDEO || s->codec_id==AV_CODEC_ID_MPEG2VIDEO)){
                put_dct(s, block[0], 0, dest_y                          , dct_linesize, s->qscale);
                put_dct(s, block[1], 1, dest_y              + block_size, dct_linesize, s->qscale);
                put_dct(s, block[2], 2, dest_y + dct_offset             , dct_linesize, s->qscale);
                put_dct(s, block[3], 3, dest_y + dct_offset + block_size, dct_linesize, s->qscale);

                if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY)){
                    if(s->chroma_y_shift){
                        put_dct(s, block[4], 4, dest_cb, uvlinesize, s->chroma_qscale);
                        put_dct(s, block[5], 5, dest_cr, uvlinesize, s->chroma_qscale);
                    }else{
                        dct_offset >>=1;
                        dct_linesize >>=1;
                        put_dct(s, block[4], 4, dest_cb,              dct_linesize, s->chroma_qscale);
                        put_dct(s, block[5], 5, dest_cr,              dct_linesize, s->chroma_qscale);
                        put_dct(s, block[6], 6, dest_cb + dct_offset, dct_linesize, s->chroma_qscale);
                        put_dct(s, block[7], 7, dest_cr + dct_offset, dct_linesize, s->chroma_qscale);
                    }
                }
            }else{
                s->dsp.idct_put(dest_y                          , dct_linesize, block[0]);
                s->dsp.idct_put(dest_y              + block_size, dct_linesize, block[1]);
                s->dsp.idct_put(dest_y + dct_offset             , dct_linesize, block[2]);
                s->dsp.idct_put(dest_y + dct_offset + block_size, dct_linesize, block[3]);

                if(!CONFIG_GRAY || !(s->flags&CODEC_FLAG_GRAY)){
                    if(s->chroma_y_shift){
                        s->dsp.idct_put(dest_cb, uvlinesize, block[4]);
                        s->dsp.idct_put(dest_cr, uvlinesize, block[5]);
                    }else{

                        dct_linesize = uvlinesize << s->interlaced_dct;
                        dct_offset   = s->interlaced_dct? uvlinesize : uvlinesize*block_size;

                        s->dsp.idct_put(dest_cb,              dct_linesize, block[4]);
                        s->dsp.idct_put(dest_cr,              dct_linesize, block[5]);
                        s->dsp.idct_put(dest_cb + dct_offset, dct_linesize, block[6]);
                        s->dsp.idct_put(dest_cr + dct_offset, dct_linesize, block[7]);
                        if(!s->chroma_x_shift){//Chroma444
                            s->dsp.idct_put(dest_cb + block_size,              dct_linesize, block[8]);
                            s->dsp.idct_put(dest_cr + block_size,              dct_linesize, block[9]);
                            s->dsp.idct_put(dest_cb + block_size + dct_offset, dct_linesize, block[10]);
                            s->dsp.idct_put(dest_cr + block_size + dct_offset, dct_linesize, block[11]);
                        }
                    }
                }//gray
            }
        }
skip_idct:
        if(!readable){
            s->dsp.put_pixels_tab[0][0](s->dest[0], dest_y ,   linesize,16);
            s->dsp.put_pixels_tab[s->chroma_x_shift][0](s->dest[1], dest_cb, uvlinesize,16 >> s->chroma_y_shift);
            s->dsp.put_pixels_tab[s->chroma_x_shift][0](s->dest[2], dest_cr, uvlinesize,16 >> s->chroma_y_shift);
        }
    }
}

void ff_MPV_decode_mb(MpegEncContext *s, int16_t block[12][64]){
#if !CONFIG_SMALL
    if(s->out_format == FMT_MPEG1) {
        if(s->avctx->lowres) MPV_decode_mb_internal(s, block, 1, 1);
        else                 MPV_decode_mb_internal(s, block, 0, 1);
    } else
#endif
    if(s->avctx->lowres) MPV_decode_mb_internal(s, block, 1, 0);
    else                  MPV_decode_mb_internal(s, block, 0, 0);
}

static int sse(MpegEncContext *s, uint8_t *src1, uint8_t *src2, int w, int h, int stride){
	LOGD("sse Called ff_squareTbl\n");
	uint32_t *sq = ff_squareTbl + 256;
    int acc=0;
    int x,y;

    if(w==16 && h==16)
        return s->dsp.sse[0](NULL, src1, src2, stride, 16);
    else if(w==8 && h==8)
        return s->dsp.sse[1](NULL, src1, src2, stride, 8);

    for(y=0; y<h; y++){
        for(x=0; x<w; x++){
            acc+= sq[src1[x + y*stride] - src2[x + y*stride]];
        }
    }

//    av_assert2(acc>=0);

    return acc;
}

//static int sse_mb(MpegEncContext *s){
//    int w= 16;
//    int h= 16;
//
//    if(s->mb_x*16 + 16 > s->width ) w= s->width - s->mb_x*16;
//    if(s->mb_y*16 + 16 > s->height) h= s->height- s->mb_y*16;
//
//    if(w==16 && h==16)
//      if(s->avctx->mb_cmp == FF_CMP_NSSE){
//        return  s->dsp.nsse[0](s, s->new_picture.f.data[0] + s->mb_x*16 + s->mb_y*s->linesize*16, s->dest[0], s->linesize, 16)
//               +s->dsp.nsse[1](s, s->new_picture.f.data[1] + s->mb_x*8  + s->mb_y*s->uvlinesize*8,s->dest[1], s->uvlinesize, 8)
//               +s->dsp.nsse[1](s, s->new_picture.f.data[2] + s->mb_x*8  + s->mb_y*s->uvlinesize*8,s->dest[2], s->uvlinesize, 8);
//      }else{
//        return  s->dsp.sse[0](NULL, s->new_picture.f.data[0] + s->mb_x*16 + s->mb_y*s->linesize*16, s->dest[0], s->linesize, 16)
//               +s->dsp.sse[1](NULL, s->new_picture.f.data[1] + s->mb_x*8  + s->mb_y*s->uvlinesize*8,s->dest[1], s->uvlinesize, 8)
//               +s->dsp.sse[1](NULL, s->new_picture.f.data[2] + s->mb_x*8  + s->mb_y*s->uvlinesize*8,s->dest[2], s->uvlinesize, 8);
//      }
//    else
//        return  sse(s, s->new_picture.f.data[0] + s->mb_x*16 + s->mb_y*s->linesize*16, s->dest[0], w, h, s->linesize)
//               +sse(s, s->new_picture.f.data[1] + s->mb_x*8  + s->mb_y*s->uvlinesize*8,s->dest[1], w>>1, h>>1, s->uvlinesize)
//               +sse(s, s->new_picture.f.data[2] + s->mb_x*8  + s->mb_y*s->uvlinesize*8,s->dest[2], w>>1, h>>1, s->uvlinesize);
//}


static inline void copy_context_after_encode(MpegEncContext *d, MpegEncContext *s, int type){
    int i;

    memcpy(d->mv, s->mv, 2*4*2*sizeof(int));
    memcpy(d->last_mv, s->last_mv, 2*2*2*sizeof(int)); //FIXME is memcpy faster than a loop?

    /* mpeg1 */
    d->mb_skip_run= s->mb_skip_run;
    for(i=0; i<3; i++)
        d->last_dc[i] = s->last_dc[i];

    /* statistics */
    d->mv_bits= s->mv_bits;
    d->i_tex_bits= s->i_tex_bits;
    d->p_tex_bits= s->p_tex_bits;
    d->i_count= s->i_count;
    d->f_count= s->f_count;
    d->b_count= s->b_count;
    d->skip_count= s->skip_count;
    d->misc_bits= s->misc_bits;

    d->mb_intra= s->mb_intra;
    d->mb_skipped= s->mb_skipped;
    d->mv_type= s->mv_type;
    d->mv_dir= s->mv_dir;
    d->pb= s->pb;
    if(s->data_partitioning){
        d->pb2= s->pb2;
        d->tex_pb= s->tex_pb;
    }
    d->block= s->block;
    for(i=0; i<8; i++)
        d->block_last_index[i]= s->block_last_index[i];
    d->interlaced_dct= s->interlaced_dct;
    d->qscale= s->qscale;

    d->esc3_level_length= s->esc3_level_length;
}

//static inline void encode_mb_hq(MpegEncContext *s, MpegEncContext *backup, MpegEncContext *best, int type,
//                           PutBitContext pb[2], PutBitContext pb2[2], PutBitContext tex_pb[2],
//                           int *dmin, int *next_block, int motion_x, int motion_y)
//{
//    int score;
//    uint8_t *dest_backup[3];
//
//    copy_context_before_encode(s, backup, type);
//
//    s->block= s->blocks[*next_block];
//    s->pb= pb[*next_block];
//    if(s->data_partitioning){
//        s->pb2   = pb2   [*next_block];
//        s->tex_pb= tex_pb[*next_block];
//    }
//
//    if(*next_block){
//        memcpy(dest_backup, s->dest, sizeof(s->dest));
//        s->dest[0] = s->rd_scratchpad;
//        s->dest[1] = s->rd_scratchpad + 16*s->linesize;
//        s->dest[2] = s->rd_scratchpad + 16*s->linesize + 8;
////        assert(s->linesize >= 32); //FIXME
//    }
//
//    encode_mb(s, motion_x, motion_y);
//
//    score= put_bits_count(&s->pb);
//    if(s->data_partitioning){
//        score+= put_bits_count(&s->pb2);
//        score+= put_bits_count(&s->tex_pb);
//    }
//
//    if(s->avctx->mb_decision == FF_MB_DECISION_RD){
//        ff_MPV_decode_mb(s, s->block);
//
//        score *= s->lambda2;
//        score += sse_mb(s) << FF_LAMBDA_SHIFT;
//    }
//
//    if(*next_block){
//        memcpy(s->dest, dest_backup, sizeof(s->dest));
//    }
//
//    if(score<*dmin){
//        *dmin= score;
//        *next_block^=1;
//
//        copy_context_after_encode(best, s, type);
//    }
//}


static inline void ff_mpeg4_set_one_direct_mv(MpegEncContext *s, int mx, int my, int i){
    int xy= s->block_index[i];
    uint16_t time_pp= s->pp_time;
    uint16_t time_pb= s->pb_time;
    int p_mx, p_my;

    p_mx = s->next_picture.f.motion_val[0][xy][0];
    if((unsigned)(p_mx + tab_bias) < tab_size){
        s->mv[0][i][0] = s->direct_scale_mv[0][p_mx + tab_bias] + mx;
        s->mv[1][i][0] = mx ? s->mv[0][i][0] - p_mx
                            : s->direct_scale_mv[1][p_mx + tab_bias];
    }else{
        s->mv[0][i][0] = p_mx*time_pb/time_pp + mx;
        s->mv[1][i][0] = mx ? s->mv[0][i][0] - p_mx
                            : p_mx*(time_pb - time_pp)/time_pp;
    }
    p_my = s->next_picture.f.motion_val[0][xy][1];
    if((unsigned)(p_my + tab_bias) < tab_size){
        s->mv[0][i][1] = s->direct_scale_mv[0][p_my + tab_bias] + my;
        s->mv[1][i][1] = my ? s->mv[0][i][1] - p_my
                            : s->direct_scale_mv[1][p_my + tab_bias];
    }else{
        s->mv[0][i][1] = p_my*time_pb/time_pp + my;
        s->mv[1][i][1] = my ? s->mv[0][i][1] - p_my
                            : p_my*(time_pb - time_pp)/time_pp;
    }
}


int ff_mpeg4_set_direct_mv(MpegEncContext *s, int mx, int my){
    const int mb_index= s->mb_x + s->mb_y*s->mb_stride;
    const int colocated_mb_type = s->next_picture.f.mb_type[mb_index];
    uint16_t time_pp;
    uint16_t time_pb;
    int i;

    //FIXME avoid divides
    // try special case with shifts for 1 and 3 B-frames?

    if(IS_8X8(colocated_mb_type)){
        s->mv_type = MV_TYPE_8X8;
        for(i=0; i<4; i++){
            ff_mpeg4_set_one_direct_mv(s, mx, my, i);
        }
        return MB_TYPE_DIRECT2 | MB_TYPE_8x8 | MB_TYPE_L0L1;
    } else if(IS_INTERLACED(colocated_mb_type)){
        s->mv_type = MV_TYPE_FIELD;
        for(i=0; i<2; i++){
            int field_select = s->next_picture.f.ref_index[0][4 * mb_index + 2 * i];
            s->field_select[0][i]= field_select;
            s->field_select[1][i]= i;
            if(s->top_field_first){
                time_pp= s->pp_field_time - field_select + i;
                time_pb= s->pb_field_time - field_select + i;
            }else{
                time_pp= s->pp_field_time + field_select - i;
                time_pb= s->pb_field_time + field_select - i;
            }
            s->mv[0][i][0] = s->p_field_mv_table[i][0][mb_index][0]*time_pb/time_pp + mx;
            s->mv[0][i][1] = s->p_field_mv_table[i][0][mb_index][1]*time_pb/time_pp + my;
            s->mv[1][i][0] = mx ? s->mv[0][i][0] - s->p_field_mv_table[i][0][mb_index][0]
                                : s->p_field_mv_table[i][0][mb_index][0]*(time_pb - time_pp)/time_pp;
            s->mv[1][i][1] = my ? s->mv[0][i][1] - s->p_field_mv_table[i][0][mb_index][1]
                                : s->p_field_mv_table[i][0][mb_index][1]*(time_pb - time_pp)/time_pp;
        }
        return MB_TYPE_DIRECT2 | MB_TYPE_16x8 | MB_TYPE_L0L1 | MB_TYPE_INTERLACED;
    }else{
        ff_mpeg4_set_one_direct_mv(s, mx, my, 0);
        s->mv[0][1][0] = s->mv[0][2][0] = s->mv[0][3][0] = s->mv[0][0][0];
        s->mv[0][1][1] = s->mv[0][2][1] = s->mv[0][3][1] = s->mv[0][0][1];
        s->mv[1][1][0] = s->mv[1][2][0] = s->mv[1][3][0] = s->mv[1][0][0];
        s->mv[1][1][1] = s->mv[1][2][1] = s->mv[1][3][1] = s->mv[1][0][1];
        if((s->avctx->workaround_bugs & FF_BUG_DIRECT_BLOCKSIZE) || !s->quarter_sample)
            s->mv_type= MV_TYPE_16X16;
        else
            s->mv_type= MV_TYPE_8X8;
        return MB_TYPE_DIRECT2 | MB_TYPE_16x16 | MB_TYPE_L0L1; //Note see prev line
    }
}



void ff_h263_update_motion_val(MpegEncContext * s){
    const int mb_xy = s->mb_y * s->mb_stride + s->mb_x;
               //FIXME a lot of that is only needed for !low_delay
    const int wrap = s->b8_stride;
    const int xy = s->block_index[0];

    s->current_picture.f.mbskip_table[mb_xy] = s->mb_skipped;

    if(s->mv_type != MV_TYPE_8X8){
        int motion_x, motion_y;
        if (s->mb_intra) {
            motion_x = 0;
            motion_y = 0;
        } else if (s->mv_type == MV_TYPE_16X16) {
            motion_x = s->mv[0][0][0];
            motion_y = s->mv[0][0][1];
        } else /*if (s->mv_type == MV_TYPE_FIELD)*/ {
            int i;
            motion_x = s->mv[0][0][0] + s->mv[0][1][0];
            motion_y = s->mv[0][0][1] + s->mv[0][1][1];
            motion_x = (motion_x>>1) | (motion_x&1);
            for(i=0; i<2; i++){
                s->p_field_mv_table[i][0][mb_xy][0]= s->mv[0][i][0];
                s->p_field_mv_table[i][0][mb_xy][1]= s->mv[0][i][1];
            }
            s->current_picture.f.ref_index[0][4*mb_xy    ] =
            s->current_picture.f.ref_index[0][4*mb_xy + 1] = s->field_select[0][0];
            s->current_picture.f.ref_index[0][4*mb_xy + 2] =
            s->current_picture.f.ref_index[0][4*mb_xy + 3] = s->field_select[0][1];
        }

        /* no update if 8X8 because it has been done during parsing */
        s->current_picture.f.motion_val[0][xy][0]            = motion_x;
        s->current_picture.f.motion_val[0][xy][1]            = motion_y;
        s->current_picture.f.motion_val[0][xy + 1][0]        = motion_x;
        s->current_picture.f.motion_val[0][xy + 1][1]        = motion_y;
        s->current_picture.f.motion_val[0][xy + wrap][0]     = motion_x;
        s->current_picture.f.motion_val[0][xy + wrap][1]     = motion_y;
        s->current_picture.f.motion_val[0][xy + 1 + wrap][0] = motion_x;
        s->current_picture.f.motion_val[0][xy + 1 + wrap][1] = motion_y;
    }

    if(s->encoding){ //FIXME encoding MUST be cleaned up
        if (s->mv_type == MV_TYPE_8X8)
            s->current_picture.f.mb_type[mb_xy] = MB_TYPE_L0 | MB_TYPE_8x8;
        else if(s->mb_intra)
            s->current_picture.f.mb_type[mb_xy] = MB_TYPE_INTRA;
        else
            s->current_picture.f.mb_type[mb_xy] = MB_TYPE_L0 | MB_TYPE_16x16;
    }
}


void ff_h263_loop_filter(MpegEncContext * s){
    int qp_c;
    const int linesize  = s->linesize;
    const int uvlinesize= s->uvlinesize;
    const int xy = s->mb_y * s->mb_stride + s->mb_x;
    uint8_t *dest_y = s->dest[0];
    uint8_t *dest_cb= s->dest[1];
    uint8_t *dest_cr= s->dest[2];

//    if(s->pict_type==AV_PICTURE_TYPE_B && !s->readable) return;

    /*
       Diag Top
       Left Center
    */
    if (!IS_SKIP(s->current_picture.f.mb_type[xy])) {
        qp_c= s->qscale;
        s->dsp.h263_v_loop_filter(dest_y+8*linesize  , linesize, qp_c);
        s->dsp.h263_v_loop_filter(dest_y+8*linesize+8, linesize, qp_c);
    }else
        qp_c= 0;

    if(s->mb_y){
        int qp_dt, qp_tt, qp_tc;

        if (IS_SKIP(s->current_picture.f.mb_type[xy - s->mb_stride]))
            qp_tt=0;
        else
            qp_tt = s->current_picture.f.qscale_table[xy - s->mb_stride];

        if(qp_c)
            qp_tc= qp_c;
        else
            qp_tc= qp_tt;

        if(qp_tc){
            const int chroma_qp= s->chroma_qscale_table[qp_tc];
            s->dsp.h263_v_loop_filter(dest_y  ,   linesize, qp_tc);
            s->dsp.h263_v_loop_filter(dest_y+8,   linesize, qp_tc);

            s->dsp.h263_v_loop_filter(dest_cb , uvlinesize, chroma_qp);
            s->dsp.h263_v_loop_filter(dest_cr , uvlinesize, chroma_qp);
        }

        if(qp_tt)
            s->dsp.h263_h_loop_filter(dest_y-8*linesize+8  ,   linesize, qp_tt);

        if(s->mb_x){
            if (qp_tt || IS_SKIP(s->current_picture.f.mb_type[xy - 1 - s->mb_stride]))
                qp_dt= qp_tt;
            else
                qp_dt = s->current_picture.f.qscale_table[xy - 1 - s->mb_stride];

            if(qp_dt){
                const int chroma_qp= s->chroma_qscale_table[qp_dt];
                s->dsp.h263_h_loop_filter(dest_y -8*linesize  ,   linesize, qp_dt);
                s->dsp.h263_h_loop_filter(dest_cb-8*uvlinesize, uvlinesize, chroma_qp);
                s->dsp.h263_h_loop_filter(dest_cr-8*uvlinesize, uvlinesize, chroma_qp);
            }
        }
    }

    if(qp_c){
        s->dsp.h263_h_loop_filter(dest_y +8,   linesize, qp_c);
        if(s->mb_y + 1 == s->mb_height)
            s->dsp.h263_h_loop_filter(dest_y+8*linesize+8,   linesize, qp_c);
    }

    if(s->mb_x){
        int qp_lc;
        if (qp_c || IS_SKIP(s->current_picture.f.mb_type[xy - 1]))
            qp_lc= qp_c;
        else
            qp_lc = s->current_picture.f.qscale_table[xy - 1];

        if(qp_lc){
            s->dsp.h263_h_loop_filter(dest_y,   linesize, qp_lc);
            if(s->mb_y + 1 == s->mb_height){
                const int chroma_qp= s->chroma_qscale_table[qp_lc];
                s->dsp.h263_h_loop_filter(dest_y +8*  linesize,   linesize, qp_lc);
                s->dsp.h263_h_loop_filter(dest_cb             , uvlinesize, chroma_qp);
                s->dsp.h263_h_loop_filter(dest_cr             , uvlinesize, chroma_qp);
            }
        }
    }
}


void ff_msmpeg4_encode_ext_header(MpegEncContext * s)
{
        unsigned fps = s->avctx->time_base.den / s->avctx->time_base.num / FFMAX(s->avctx->ticks_per_frame, 1);
        put_bits(&s->pb, 5, FFMIN(fps, 31)); //yes 29.97 -> 29

        put_bits(&s->pb, 11, FFMIN(s->bit_rate/1024, 2047));

        if(s->msmpeg4_version>=3)
            put_bits(&s->pb, 1, s->flipflop_rounding);
//        else
//            av_assert0(s->flipflop_rounding==0);
}

//実データをエンコードする関数
static int encode_thread(AVCodecContext *c, void *arg){
//	LOGD("encode_thread Called\n");
    MpegEncContext *s= *(void**)arg;
    int mb_x, mb_y, pdif = 0;
    int chr_h= 16>>s->chroma_y_shift;
    int i, j;
    MpegEncContext best_s, backup_s;
    uint8_t bit_buf[2][MAX_MB_BYTES];
    uint8_t bit_buf2[2][MAX_MB_BYTES];
    uint8_t bit_buf_tex[2][MAX_MB_BYTES];
    PutBitContext pb[2], pb2[2], tex_pb[2];

    ff_check_alignment();

    for(i=0; i<2; i++){
        init_put_bits(&pb    [i], bit_buf    [i], MAX_MB_BYTES);
        init_put_bits(&pb2   [i], bit_buf2   [i], MAX_MB_BYTES);
        init_put_bits(&tex_pb[i], bit_buf_tex[i], MAX_MB_BYTES);
    }

    s->last_bits= put_bits_count(&s->pb);
    s->mv_bits=0;
    s->misc_bits=0;
    s->i_tex_bits=0;
    s->p_tex_bits=0;
    s->i_count=0;
    s->f_count=0;
    s->b_count=0;
    s->skip_count=0;

    for(i=0; i<3; i++){
        /* init last dc values */
        /* note: quant matrix value (8) is implied here */
        s->last_dc[i] = 128 << s->intra_dc_precision;

        s->current_picture.f.error[i] = 0;
    }
    if(s->codec_id==AV_CODEC_ID_AMV){
        s->last_dc[0] = 128*8/13;
        s->last_dc[1] = 128*8/14;
        s->last_dc[2] = 128*8/14;
    }
    s->mb_skip_run = 0;
    memset(s->last_mv, 0, sizeof(s->last_mv));
    s->last_mv_dir = 0;

    s->gob_index = ff_h263_get_gob_height(s);

    s->resync_mb_x=0;
    s->resync_mb_y=0;
    s->first_slice_line = 1;
    s->ptr_lastgob = s->pb.buf;
    for(mb_y= s->start_mb_y; mb_y < s->end_mb_y; mb_y++) {//-------------------Y
//    	LOGD("encode_thread mb loop mb_y:%d\n",mb_y);
        s->mb_x=0;
        s->mb_y= mb_y;

        ff_set_qscale(s, s->qscale);
        ff_init_block_index(s);

        for(mb_x=0; mb_x < s->mb_width; mb_x++) {//-------------------X
            int xy= mb_y*s->mb_stride + mb_x; // removed const, H261 needs to adjust this
            int mb_type= s->mb_type[xy];
//            int d;
            int dmin= INT_MAX;
            int dir;

            if(s->pb.buf_end - s->pb.buf - (put_bits_count(&s->pb)>>3) < MAX_MB_BYTES){
                printf( "encoded frame too large\n");
                return -1;
            }
            if(s->data_partitioning){
                if(   s->pb2   .buf_end - s->pb2   .buf - (put_bits_count(&s->    pb2)>>3) < MAX_MB_BYTES
                   || s->tex_pb.buf_end - s->tex_pb.buf - (put_bits_count(&s->tex_pb )>>3) < MAX_MB_BYTES){
                    printf( "encoded partitioned frame too large\n");
                    return -1;
                }
            }

            s->mb_x = mb_x;
            s->mb_y = mb_y;  // moved into loop, can get changed by H.261
            ff_update_block_index(s);

             if(CONFIG_H261_ENCODER && s->codec_id == AV_CODEC_ID_H261){//実行されないと思われるので省略
            	LOGD( "It's H261 wrong --------------- ");
            	//                ff_h261_reorder_mb_index(s);
//                xy= s->mb_y*s->mb_stride + s->mb_x;
//                mb_type= s->mb_type[xy];
            }

            /* write gob / video packet header  */
            if(s->rtp_mode){
                int current_packet_size, is_gob_start;

                current_packet_size= ((put_bits_count(&s->pb)+7)>>3) - (s->ptr_lastgob - s->pb.buf);

                is_gob_start= s->avctx->rtp_payload_size && current_packet_size >= s->avctx->rtp_payload_size && mb_y + mb_x>0;

                if(s->start_mb_y == mb_y && mb_y > 0 && mb_x==0) is_gob_start=1;

                if(!s->h263_slice_structured)
                    if(s->mb_x || s->mb_y%s->gob_index) is_gob_start=0;
//                switch(s->codec_id){
//                case AV_CODEC_ID_H263:
//                case AV_CODEC_ID_H263P:
//                    if(!s->h263_slice_structured)
//                        if(s->mb_x || s->mb_y%s->gob_index) is_gob_start=0;
//                    break;
//                case AV_CODEC_ID_MPEG2VIDEO:
//                    if(s->mb_x==0 && s->mb_y!=0) is_gob_start=1;
//                case AV_CODEC_ID_MPEG1VIDEO:
//                    if(s->mb_skip_run) is_gob_start=0;
//                    break;
//                case AV_CODEC_ID_MJPEG:
//                    if(s->mb_x==0 && s->mb_y!=0) is_gob_start=1;
//                    break;
//                }

                if(is_gob_start){
                    if(s->start_mb_y != mb_y || mb_x!=0){
                        write_slice_end(s);
                        if(CONFIG_MPEG4_ENCODER && s->codec_id==AV_CODEC_ID_MPEG4 && s->partitioned_frame){
                            ff_mpeg4_init_partitions(s);
                        }
                    }

//                    av_assert2((put_bits_count(&s->pb)&7) == 0);
                    current_packet_size= put_bits_ptr(&s->pb) - s->ptr_lastgob;

                    if(s->avctx->error_rate && s->resync_mb_x + s->resync_mb_y > 0){
                        int r= put_bits_count(&s->pb)/8 + s->picture_number + 16 + s->mb_x + s->mb_y;
                        int d= 100 / s->avctx->error_rate;
                        if(r % d == 0){
                            current_packet_size=0;
                            s->pb.buf_ptr= s->ptr_lastgob;
//                            assert(put_bits_ptr(&s->pb) == s->ptr_lastgob);
                        }
                    }

                    if (s->avctx->rtp_callback){
                        int number_mb = (mb_y - s->resync_mb_y)*s->mb_width + mb_x - s->resync_mb_x;
                        s->avctx->rtp_callback(s->avctx, s->ptr_lastgob, current_packet_size, number_mb);
                    }
                    update_mb_info(s, 1);

//                    switch(s->codec_id){
//                    case AV_CODEC_ID_MPEG4:
//                        if (CONFIG_MPEG4_ENCODER) {
//                            ff_mpeg4_encode_video_packet_header(s);
//                            ff_mpeg4_clean_buffers(s);
//                        }
//                    break;
//                    case AV_CODEC_ID_MPEG1VIDEO:
//                    case AV_CODEC_ID_MPEG2VIDEO:
//                        if (CONFIG_MPEG1VIDEO_ENCODER || CONFIG_MPEG2VIDEO_ENCODER) {//実行されないと思われるので省略
//                        	printf("It's MPEG1 wrong --------------- ");
////                            ff_mpeg1_encode_slice_header(s);
////                            ff_mpeg1_clean_buffers(s);
//                        }
//                    break;
//                    case AV_CODEC_ID_H263:
//                    case AV_CODEC_ID_H263P:
                        if (CONFIG_H263_ENCODER)
                            ff_h263_encode_gob_header(s, mb_y);
//                    break;
//                    }

                    if(s->flags&CODEC_FLAG_PASS1){
                        int bits= put_bits_count(&s->pb);
                        s->misc_bits+= bits - s->last_bits;
                        s->last_bits= bits;
                    }

                    s->ptr_lastgob += current_packet_size;
                    s->first_slice_line=1;
                    s->resync_mb_x=mb_x;
                    s->resync_mb_y=mb_y;
                }
            }

            if(  (s->resync_mb_x   == s->mb_x)
               && s->resync_mb_y+1 == s->mb_y){
                s->first_slice_line=0;
            }

            s->mb_skipped=0;
            s->dquant=0; //only for QP_RD

            update_mb_info(s, 0);

            //毎回BB
//        	if ((mb_type & (mb_type-1)) || (s->mpv_flags & FF_MPV_FLAG_QP_RD)) { // more than 1 MB type possible or FF_MPV_FLAG_QP_RD
////            	LOGD("encode_thread BA\n");
//
//            	int next_block=0;
//                int pb_bits_count, pb2_bits_count, tex_pb_bits_count;
//
//                copy_context_before_encode(&backup_s, s, -1);
//                backup_s.pb= s->pb;
//                best_s.data_partitioning= s->data_partitioning;
//                best_s.partitioned_frame= s->partitioned_frame;
//                if(s->data_partitioning){
//                    backup_s.pb2= s->pb2;
//                    backup_s.tex_pb= s->tex_pb;
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_INTER){
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_16X16;
//                    s->mb_intra= 0;
//                    s->mv[0][0][0] = s->p_mv_table[xy][0];
//                    s->mv[0][0][1] = s->p_mv_table[xy][1];
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_INTER, pb, pb2, tex_pb,
//                                 &dmin, &next_block, s->mv[0][0][0], s->mv[0][0][1]);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_INTER_I){
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_FIELD;
//                    s->mb_intra= 0;
//                    for(i=0; i<2; i++){
//                        j= s->field_select[0][i] = s->p_field_select_table[i][xy];
//                        s->mv[0][i][0] = s->p_field_mv_table[i][j][xy][0];
//                        s->mv[0][i][1] = s->p_field_mv_table[i][j][xy][1];
//                    }
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_INTER_I, pb, pb2, tex_pb,
//                                 &dmin, &next_block, 0, 0);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_SKIPPED){
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_16X16;
//                    s->mb_intra= 0;
//                    s->mv[0][0][0] = 0;
//                    s->mv[0][0][1] = 0;
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_SKIPPED, pb, pb2, tex_pb,
//                                 &dmin, &next_block, s->mv[0][0][0], s->mv[0][0][1]);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_INTER4V){
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_8X8;
//                    s->mb_intra= 0;
//                    for(i=0; i<4; i++){
//                        s->mv[0][i][0] = s->current_picture.f.motion_val[0][s->block_index[i]][0];
//                        s->mv[0][i][1] = s->current_picture.f.motion_val[0][s->block_index[i]][1];
//                    }
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_INTER4V, pb, pb2, tex_pb,
//                                 &dmin, &next_block, 0, 0);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_FORWARD){
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_16X16;
//                    s->mb_intra= 0;
//                    s->mv[0][0][0] = s->b_forw_mv_table[xy][0];
//                    s->mv[0][0][1] = s->b_forw_mv_table[xy][1];
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_FORWARD, pb, pb2, tex_pb,
//                                 &dmin, &next_block, s->mv[0][0][0], s->mv[0][0][1]);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_BACKWARD){
//                    s->mv_dir = MV_DIR_BACKWARD;
//                    s->mv_type = MV_TYPE_16X16;
//                    s->mb_intra= 0;
//                    s->mv[1][0][0] = s->b_back_mv_table[xy][0];
//                    s->mv[1][0][1] = s->b_back_mv_table[xy][1];
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_BACKWARD, pb, pb2, tex_pb,
//                                 &dmin, &next_block, s->mv[1][0][0], s->mv[1][0][1]);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_BIDIR){
//                    s->mv_dir = MV_DIR_FORWARD | MV_DIR_BACKWARD;
//                    s->mv_type = MV_TYPE_16X16;
//                    s->mb_intra= 0;
//                    s->mv[0][0][0] = s->b_bidir_forw_mv_table[xy][0];
//                    s->mv[0][0][1] = s->b_bidir_forw_mv_table[xy][1];
//                    s->mv[1][0][0] = s->b_bidir_back_mv_table[xy][0];
//                    s->mv[1][0][1] = s->b_bidir_back_mv_table[xy][1];
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_BIDIR, pb, pb2, tex_pb,
//                                 &dmin, &next_block, 0, 0);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_FORWARD_I){
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_FIELD;
//                    s->mb_intra= 0;
//                    for(i=0; i<2; i++){
//                        j= s->field_select[0][i] = s->b_field_select_table[0][i][xy];
//                        s->mv[0][i][0] = s->b_field_mv_table[0][i][j][xy][0];
//                        s->mv[0][i][1] = s->b_field_mv_table[0][i][j][xy][1];
//                    }
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_FORWARD_I, pb, pb2, tex_pb,
//                                 &dmin, &next_block, 0, 0);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_BACKWARD_I){
//                    s->mv_dir = MV_DIR_BACKWARD;
//                    s->mv_type = MV_TYPE_FIELD;
//                    s->mb_intra= 0;
//                    for(i=0; i<2; i++){
//                        j= s->field_select[1][i] = s->b_field_select_table[1][i][xy];
//                        s->mv[1][i][0] = s->b_field_mv_table[1][i][j][xy][0];
//                        s->mv[1][i][1] = s->b_field_mv_table[1][i][j][xy][1];
//                    }
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_BACKWARD_I, pb, pb2, tex_pb,
//                                 &dmin, &next_block, 0, 0);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_BIDIR_I){
//                    s->mv_dir = MV_DIR_FORWARD | MV_DIR_BACKWARD;
//                    s->mv_type = MV_TYPE_FIELD;
//                    s->mb_intra= 0;
//                    for(dir=0; dir<2; dir++){
//                        for(i=0; i<2; i++){
//                            j= s->field_select[dir][i] = s->b_field_select_table[dir][i][xy];
//                            s->mv[dir][i][0] = s->b_field_mv_table[dir][i][j][xy][0];
//                            s->mv[dir][i][1] = s->b_field_mv_table[dir][i][j][xy][1];
//                        }
//                    }
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_BIDIR_I, pb, pb2, tex_pb,
//                                 &dmin, &next_block, 0, 0);
//                }
//                if(mb_type&CANDIDATE_MB_TYPE_INTRA){
//                    s->mv_dir = 0;
//                    s->mv_type = MV_TYPE_16X16;
//                    s->mb_intra= 1;
//                    s->mv[0][0][0] = 0;
//                    s->mv[0][0][1] = 0;
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_INTRA, pb, pb2, tex_pb,
//                                 &dmin, &next_block, 0, 0);
//                    if(s->h263_pred || s->h263_aic){
//                        if(best_s.mb_intra)
//                            s->mbintra_table[mb_x + mb_y*s->mb_stride]=1;
//                        else
//                            ff_clean_intra_table_entries(s); //old mode?
//                    }
//                }
//
//                if ((s->mpv_flags & FF_MPV_FLAG_QP_RD) && dmin < INT_MAX) {
//                    if(best_s.mv_type==MV_TYPE_16X16){ //FIXME move 4mv after QPRD
//                        const int last_qp= backup_s.qscale;
//                        int qpi, qp, dc[6];
//                        int16_t ac[6][16];
//                        const int mvdir= (best_s.mv_dir&MV_DIR_BACKWARD) ? 1 : 0;
//                        static const int dquant_tab[4]={-1,1,-2,2};
//                        int storecoefs = s->mb_intra && s->dc_val[0];
//
////                        av_assert2(backup_s.dquant == 0);
//
//                        //FIXME intra
//                        s->mv_dir= best_s.mv_dir;
//                        s->mv_type = MV_TYPE_16X16;
//                        s->mb_intra= best_s.mb_intra;
//                        s->mv[0][0][0] = best_s.mv[0][0][0];
//                        s->mv[0][0][1] = best_s.mv[0][0][1];
//                        s->mv[1][0][0] = best_s.mv[1][0][0];
//                        s->mv[1][0][1] = best_s.mv[1][0][1];
//
//                        qpi = s->pict_type == AV_PICTURE_TYPE_B ? 2 : 0;
//                        for(; qpi<4; qpi++){
//                            int dquant= dquant_tab[qpi];
//                            qp= last_qp + dquant;
//                            if(qp < s->avctx->qmin || qp > s->avctx->qmax)
//                                continue;
//                            backup_s.dquant= dquant;
//                            if(storecoefs){
//                                for(i=0; i<6; i++){
//                                    dc[i]= s->dc_val[0][ s->block_index[i] ];
//                                    memcpy(ac[i], s->ac_val[0][s->block_index[i]], sizeof(int16_t)*16);
//                                }
//                            }
//
//                            encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_INTER /* wrong but unused */, pb, pb2, tex_pb,
//                                         &dmin, &next_block, s->mv[mvdir][0][0], s->mv[mvdir][0][1]);
//                            if(best_s.qscale != qp){
//                                if(storecoefs){
//                                    for(i=0; i<6; i++){
//                                        s->dc_val[0][ s->block_index[i] ]= dc[i];
//                                        memcpy(s->ac_val[0][s->block_index[i]], ac[i], sizeof(int16_t)*16);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                if(CONFIG_MPEG4_ENCODER && (mb_type&CANDIDATE_MB_TYPE_DIRECT)){
//                    int mx= s->b_direct_mv_table[xy][0];
//                    int my= s->b_direct_mv_table[xy][1];
//
//                    backup_s.dquant = 0;
//                    s->mv_dir = MV_DIR_FORWARD | MV_DIR_BACKWARD | MV_DIRECT;
//                    s->mb_intra= 0;
//                    ff_mpeg4_set_direct_mv(s, mx, my);
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_DIRECT, pb, pb2, tex_pb,
//                                 &dmin, &next_block, mx, my);
//                }
//                if(CONFIG_MPEG4_ENCODER && (mb_type&CANDIDATE_MB_TYPE_DIRECT0)){
//                    backup_s.dquant = 0;
//                    s->mv_dir = MV_DIR_FORWARD | MV_DIR_BACKWARD | MV_DIRECT;
//                    s->mb_intra= 0;
//                    ff_mpeg4_set_direct_mv(s, 0, 0);
//                    encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_DIRECT, pb, pb2, tex_pb,
//                                 &dmin, &next_block, 0, 0);
//                }
//                if (!best_s.mb_intra && (s->mpv_flags & FF_MPV_FLAG_SKIP_RD)) {
//                    int coded=0;
//                    for(i=0; i<6; i++)
//                        coded |= s->block_last_index[i];
//                    if(coded){
//                        int mx,my;
//                        memcpy(s->mv, best_s.mv, sizeof(s->mv));
//                        if(CONFIG_MPEG4_ENCODER && (best_s.mv_dir & MV_DIRECT)){
//                            mx=my=0; //FIXME find the one we actually used
//                            ff_mpeg4_set_direct_mv(s, mx, my);
//                        }else if(best_s.mv_dir&MV_DIR_BACKWARD){
//                            mx= s->mv[1][0][0];
//                            my= s->mv[1][0][1];
//                        }else{
//                            mx= s->mv[0][0][0];
//                            my= s->mv[0][0][1];
//                        }
//
//                        s->mv_dir= best_s.mv_dir;
//                        s->mv_type = best_s.mv_type;
//                        s->mb_intra= 0;
///*                        s->mv[0][0][0] = best_s.mv[0][0][0];
//                        s->mv[0][0][1] = best_s.mv[0][0][1];
//                        s->mv[1][0][0] = best_s.mv[1][0][0];
//                        s->mv[1][0][1] = best_s.mv[1][0][1];*/
//                        backup_s.dquant= 0;
//                        s->skipdct=1;
//                        encode_mb_hq(s, &backup_s, &best_s, CANDIDATE_MB_TYPE_INTER /* wrong but unused */, pb, pb2, tex_pb,
//                                        &dmin, &next_block, mx, my);
//                        s->skipdct=0;
//                    }
//                }
//
//                s->current_picture.f.qscale_table[xy] = best_s.qscale;
//
//                copy_context_after_encode(s, &best_s, -1);
//
//                pb_bits_count= put_bits_count(&s->pb);
//                flush_put_bits(&s->pb);
//                avpriv_copy_bits(&backup_s.pb, bit_buf[next_block^1], pb_bits_count);
//                s->pb= backup_s.pb;
//
//                if(s->data_partitioning){
//                    pb2_bits_count= put_bits_count(&s->pb2);
//                    flush_put_bits(&s->pb2);
//                    avpriv_copy_bits(&backup_s.pb2, bit_buf2[next_block^1], pb2_bits_count);
//                    s->pb2= backup_s.pb2;
//
//                    tex_pb_bits_count= put_bits_count(&s->tex_pb);
//                    flush_put_bits(&s->tex_pb);
//                    avpriv_copy_bits(&backup_s.tex_pb, bit_buf_tex[next_block^1], tex_pb_bits_count);
//                    s->tex_pb= backup_s.tex_pb;
//                }
//                s->last_bits= put_bits_count(&s->pb);
//
//                if (CONFIG_H263_ENCODER &&
//                    s->out_format == FMT_H263 && s->pict_type!=AV_PICTURE_TYPE_B)
//                    ff_h263_update_motion_val(s);
//
//                if(next_block==0){ //FIXME 16 vs linesize16
//                    s->dsp.put_pixels_tab[0][0](s->dest[0], s->rd_scratchpad                     , s->linesize  ,16);
//                    s->dsp.put_pixels_tab[1][0](s->dest[1], s->rd_scratchpad + 16*s->linesize    , s->uvlinesize, 8);
//                    s->dsp.put_pixels_tab[1][0](s->dest[2], s->rd_scratchpad + 16*s->linesize + 8, s->uvlinesize, 8);
//                }
//
//                if(s->avctx->mb_decision == FF_MB_DECISION_BITS)
//                    ff_MPV_decode_mb(s, s->block);
//            } else {//毎回通る mb_typeは毎回1
            	//            	av_log(NULL,AV_LOG_DEBUG,"encode_thread BB\n");
                int motion_x = 0, motion_y = 0;
                s->mv_type=MV_TYPE_16X16;
                // only one MB-Type possible

//            	LOGD("encode_thread BB start pbc:%d mb_type:%d\n",put_bits_count(&(s->pb)),mb_type);
//                switch(mb_type){
//                case CANDIDATE_MB_TYPE_INTRA://ここが実行される
                    s->mv_dir = 0;
                    s->mb_intra= 1;
                    motion_x= s->mv[0][0][0] = 0;
                    motion_y= s->mv[0][0][1] = 0;
//                    break;
//                case CANDIDATE_MB_TYPE_INTER:
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mb_intra= 0;
//                    motion_x= s->mv[0][0][0] = s->p_mv_table[xy][0];
//                    motion_y= s->mv[0][0][1] = s->p_mv_table[xy][1];
//                    break;
//                case CANDIDATE_MB_TYPE_INTER_I:
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_FIELD;
//                    s->mb_intra= 0;
//                    for(i=0; i<2; i++){
//                        j= s->field_select[0][i] = s->p_field_select_table[i][xy];
//                        s->mv[0][i][0] = s->p_field_mv_table[i][j][xy][0];
//                        s->mv[0][i][1] = s->p_field_mv_table[i][j][xy][1];
//                    }
//                    break;
//                case CANDIDATE_MB_TYPE_INTER4V:
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_8X8;
//                    s->mb_intra= 0;
//                    for(i=0; i<4; i++){
//                        s->mv[0][i][0] = s->current_picture.f.motion_val[0][s->block_index[i]][0];
//                        s->mv[0][i][1] = s->current_picture.f.motion_val[0][s->block_index[i]][1];
//                    }
//                    break;
//                case CANDIDATE_MB_TYPE_DIRECT:
//                    if (CONFIG_MPEG4_ENCODER) {
//                        s->mv_dir = MV_DIR_FORWARD|MV_DIR_BACKWARD|MV_DIRECT;
//                        s->mb_intra= 0;
//                        motion_x=s->b_direct_mv_table[xy][0];
//                        motion_y=s->b_direct_mv_table[xy][1];
//                        ff_mpeg4_set_direct_mv(s, motion_x, motion_y);
//                    }
//                    break;
//                case CANDIDATE_MB_TYPE_DIRECT0:
//                    if (CONFIG_MPEG4_ENCODER) {
//                        s->mv_dir = MV_DIR_FORWARD|MV_DIR_BACKWARD|MV_DIRECT;
//                        s->mb_intra= 0;
//                        ff_mpeg4_set_direct_mv(s, 0, 0);
//                    }
//                    break;
//                case CANDIDATE_MB_TYPE_BIDIR:
//                    s->mv_dir = MV_DIR_FORWARD | MV_DIR_BACKWARD;
//                    s->mb_intra= 0;
//                    s->mv[0][0][0] = s->b_bidir_forw_mv_table[xy][0];
//                    s->mv[0][0][1] = s->b_bidir_forw_mv_table[xy][1];
//                    s->mv[1][0][0] = s->b_bidir_back_mv_table[xy][0];
//                    s->mv[1][0][1] = s->b_bidir_back_mv_table[xy][1];
//                    break;
//                case CANDIDATE_MB_TYPE_BACKWARD:
//                    s->mv_dir = MV_DIR_BACKWARD;
//                    s->mb_intra= 0;
//                    motion_x= s->mv[1][0][0] = s->b_back_mv_table[xy][0];
//                    motion_y= s->mv[1][0][1] = s->b_back_mv_table[xy][1];
//                    break;
//                case CANDIDATE_MB_TYPE_FORWARD:
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mb_intra= 0;
//                    motion_x= s->mv[0][0][0] = s->b_forw_mv_table[xy][0];
//                    motion_y= s->mv[0][0][1] = s->b_forw_mv_table[xy][1];
//                    break;
//                case CANDIDATE_MB_TYPE_FORWARD_I:
//                    s->mv_dir = MV_DIR_FORWARD;
//                    s->mv_type = MV_TYPE_FIELD;
//                    s->mb_intra= 0;
//                    for(i=0; i<2; i++){
//                        j= s->field_select[0][i] = s->b_field_select_table[0][i][xy];
//                        s->mv[0][i][0] = s->b_field_mv_table[0][i][j][xy][0];
//                        s->mv[0][i][1] = s->b_field_mv_table[0][i][j][xy][1];
//                    }
//                    break;
//                case CANDIDATE_MB_TYPE_BACKWARD_I:
//                    s->mv_dir = MV_DIR_BACKWARD;
//                    s->mv_type = MV_TYPE_FIELD;
//                    s->mb_intra= 0;
//                    for(i=0; i<2; i++){
//                        j= s->field_select[1][i] = s->b_field_select_table[1][i][xy];
//                        s->mv[1][i][0] = s->b_field_mv_table[1][i][j][xy][0];
//                        s->mv[1][i][1] = s->b_field_mv_table[1][i][j][xy][1];
//                    }
//                    break;
//                case CANDIDATE_MB_TYPE_BIDIR_I:
//                    s->mv_dir = MV_DIR_FORWARD | MV_DIR_BACKWARD;
//                    s->mv_type = MV_TYPE_FIELD;
//                    s->mb_intra= 0;
//                    for(dir=0; dir<2; dir++){
//                        for(i=0; i<2; i++){
//                            j= s->field_select[dir][i] = s->b_field_select_table[dir][i][xy];
//                            s->mv[dir][i][0] = s->b_field_mv_table[dir][i][j][xy][0];
//                            s->mv[dir][i][1] = s->b_field_mv_table[dir][i][j][xy][1];
//                        }
//                    }
//                    break;
//                default:
//                    LOGD( "illegal MB type\n");
//                }

            	encode_mb(s, motion_x, motion_y);
//            	exit(0);
                // RAL: Update last macroblock type
                s->last_mv_dir = s->mv_dir;

                if (CONFIG_H263_ENCODER &&
                    s->out_format == FMT_H263 && s->pict_type!=AV_PICTURE_TYPE_B)
                    ff_h263_update_motion_val(s);

                ff_MPV_decode_mb(s, s->block);
//            }//End of BB

            /* clean the MV table in IPS frames for direct mode in B frames */
            if(s->mb_intra /* && I,P,S_TYPE */){
                s->p_mv_table[xy][0]=0;
                s->p_mv_table[xy][1]=0;
            }

            if(s->flags&CODEC_FLAG_PSNR){
                int w= 16;
                int h= 16;

                if(s->mb_x*16 + 16 > s->width ) w= s->width - s->mb_x*16;
                if(s->mb_y*16 + 16 > s->height) h= s->height- s->mb_y*16;

                s->current_picture.f.error[0] += sse(
                    s, s->new_picture.f.data[0] + s->mb_x*16 + s->mb_y*s->linesize*16,
                    s->dest[0], w, h, s->linesize);
                s->current_picture.f.error[1] += sse(
                    s, s->new_picture.f.data[1] + s->mb_x*8  + s->mb_y*s->uvlinesize*chr_h,
                    s->dest[1], w>>1, h>>s->chroma_y_shift, s->uvlinesize);
                s->current_picture.f.error[2] += sse(
                    s, s->new_picture.f.data[2] + s->mb_x*8  + s->mb_y*s->uvlinesize*chr_h,
                    s->dest[2], w>>1, h>>s->chroma_y_shift, s->uvlinesize);
            }
            if(s->loop_filter){
                if(CONFIG_H263_ENCODER && s->out_format == FMT_H263)
                    ff_h263_loop_filter(s);
            }
//            av_dlog(s->avctx, "MB %d %d bits\n",
//                    s->mb_x + s->mb_y * s->mb_stride, put_bits_count(&s->pb));


//            LOGD( "encode_thread OUT MID byte_buffer_size:%d\n",s->avctx->internal->byte_buffer_size);
//            if(s->avctx->internal->byte_buffer_size){
//                       for(i = 0; i < 100 && i< s->avctx->internal->byte_buffer_size; i++){
//                    	   LOGD( "%02X ",s->avctx->internal->byte_buffer[i]);
//                       }
//                       LOGD("\n");
//                   }
        }//End of mp loop x
    }//End of mp loop y

    //not beautiful here but we must write it before flushing so it has to be here
    if (CONFIG_MSMPEG4_ENCODER && s->msmpeg4_version && s->msmpeg4_version<4 && s->pict_type == AV_PICTURE_TYPE_I)
        ff_msmpeg4_encode_ext_header(s);

    write_slice_end(s);
    /* Send the last GOB if RTP */
    if (s->avctx->rtp_callback) {
        int number_mb = (mb_y - s->resync_mb_y)*s->mb_width - s->resync_mb_x;
        pdif = put_bits_ptr(&s->pb) - s->ptr_lastgob;
        /* Call the RTP callback to send the last GOB */
        s->avctx->rtp_callback(s->avctx, s->ptr_lastgob, pdif, number_mb);
    }

    return 0;
}

/* find best f_code for ME which do unlimited searches */
int ff_get_best_fcode(MpegEncContext * s, int16_t (*mv_table)[2], int type)
{
    if(s->me_method>=ME_EPZS){
        int score[8];
        int i, y, range= s->avctx->me_range ? s->avctx->me_range : (INT_MAX/2);
        uint8_t * fcode_tab= s->fcode_tab;
        int best_fcode=-1;
        int best_score=-10000000;

        if(s->msmpeg4_version)
            range= FFMIN(range, 16);
        else if(s->codec_id == AV_CODEC_ID_MPEG2VIDEO && s->avctx->strict_std_compliance >= FF_COMPLIANCE_NORMAL)
            range= FFMIN(range, 256);

        for(i=0; i<8; i++) score[i]= s->mb_num*(8-i);

        for(y=0; y<s->mb_height; y++){
            int x;
            int xy= y*s->mb_stride;
            for(x=0; x<s->mb_width; x++){
                if(s->mb_type[xy] & type){
                    int mx= mv_table[xy][0];
                    int my= mv_table[xy][1];
                    int fcode= FFMAX(fcode_tab[mx + MAX_MV],
                                     fcode_tab[my + MAX_MV]);
                    int j;

                        if(mx >= range || mx < -range ||
                           my >= range || my < -range)
                            continue;

                    for(j=0; j<fcode && j<8; j++){
                        if(s->pict_type==AV_PICTURE_TYPE_B || s->current_picture.mc_mb_var[xy] < s->current_picture.mb_var[xy])
                            score[j]-= 170;
                    }
                }
                xy++;
            }
        }

        for(i=1; i<8; i++){
            if(score[i] > best_score){
                best_score= score[i];
                best_fcode= i;
            }
        }

        return best_fcode;
    }else{
        return 1;
    }
}

/**
 * Get the qmin & qmax for pict_type.
 */
static void get_qminmax(int *qmin_ret, int *qmax_ret, MpegEncContext *s, int pict_type)
{
    int qmin = s->avctx->lmin;
    int qmax = s->avctx->lmax;

//    assert(qmin <= qmax);

    switch (pict_type) {
    case AV_PICTURE_TYPE_B:
        qmin = (int)(qmin * FFABS(s->avctx->b_quant_factor) + s->avctx->b_quant_offset + 0.5);
        qmax = (int)(qmax * FFABS(s->avctx->b_quant_factor) + s->avctx->b_quant_offset + 0.5);
        break;
    case AV_PICTURE_TYPE_I:
        qmin = (int)(qmin * FFABS(s->avctx->i_quant_factor) + s->avctx->i_quant_offset + 0.5);
        qmax = (int)(qmax * FFABS(s->avctx->i_quant_factor) + s->avctx->i_quant_offset + 0.5);
        break;
    }

    qmin = av_clip_c(qmin, 1, FF_LAMBDA_MAX);
    qmax = av_clip_c(qmax, 1, FF_LAMBDA_MAX);

    if (qmax < qmin)
        qmax = qmin;

    *qmin_ret = qmin;
    *qmax_ret = qmax;
}



static void update_predictor(Predictor *p, double q, double var, double size)
{
    double new_coeff = size * q / (var + 1);
    if (var < 10)
        return;

    p->count *= p->decay;
    p->coeff *= p->decay;
    p->count++;
    p->coeff += new_coeff;
}

// 1 Pass Code

static double predict_size(Predictor *p, double q, double var)
{
//	LOGD("predict_size Called p->coeff:%e p->count:%e q:%e var:%e\n",p->coeff,p->count,q,var);
    return p->coeff * var / (q * p->count);
}

static double get_diff_limited_q(MpegEncContext *s, RateControlEntry *rce, double q)
{
    RateControlContext *rcc   = &s->rc_context;
    AVCodecContext *a         = s->avctx;
    const int pict_type       = rce->new_pict_type;
    const double last_p_q     = rcc->last_qscale_for[AV_PICTURE_TYPE_P];
    const double last_non_b_q = rcc->last_qscale_for[rcc->last_non_b_pict_type];

    if (pict_type == AV_PICTURE_TYPE_I &&
        (a->i_quant_factor > 0.0 || rcc->last_non_b_pict_type == AV_PICTURE_TYPE_P))
        q = last_p_q * FFABS(a->i_quant_factor) + a->i_quant_offset;
    else if (pict_type == AV_PICTURE_TYPE_B &&
             a->b_quant_factor > 0.0)
        q = last_non_b_q * a->b_quant_factor + a->b_quant_offset;
    if (q < 1)
        q = 1;

    /* last qscale / qdiff stuff */
    if (rcc->last_non_b_pict_type == pict_type || pict_type != AV_PICTURE_TYPE_I) {
        double last_q     = rcc->last_qscale_for[pict_type];
        const int maxdiff = FF_QP2LAMBDA * a->max_qdiff;

        if (q > last_q + maxdiff)
            q = last_q + maxdiff;
        else if (q < last_q - maxdiff)
            q = last_q - maxdiff;
    }

    rcc->last_qscale_for[pict_type] = q; // Note we cannot do that after blurring

    if (pict_type != AV_PICTURE_TYPE_B)
        rcc->last_non_b_pict_type = pict_type;

    return q;
}


static double modify_qscale(MpegEncContext *s, RateControlEntry *rce,
                            double q, int frame_num)
{
    RateControlContext *rcc  = &s->rc_context;
    const double buffer_size = s->avctx->rc_buffer_size;
    const double fps         = get_fps(s->avctx);
    const double min_rate    = s->avctx->rc_min_rate / fps;
    const double max_rate    = s->avctx->rc_max_rate / fps;
    const int pict_type      = rce->new_pict_type;
    int qmin, qmax;

    get_qminmax(&qmin, &qmax, s, pict_type);

    /* modulation */
    if (s->avctx->rc_qmod_freq &&
        frame_num % s->avctx->rc_qmod_freq == 0 &&
        pict_type == AV_PICTURE_TYPE_P)
        q *= s->avctx->rc_qmod_amp;

    /* buffer overflow/underflow protection */
    if (buffer_size) {
        double expected_size = rcc->buffer_index;
        double q_limit;

        if (min_rate) {
            double d = 2 * (buffer_size - expected_size) / buffer_size;
            if (d > 1.0)
                d = 1.0;
            else if (d < 0.0001)
                d = 0.0001;
            q *= pow(d, 1.0 / s->avctx->rc_buffer_aggressivity);

            q_limit = bits2qp(rce,
                              FFMAX((min_rate - buffer_size + rcc->buffer_index) *
                                    s->avctx->rc_min_vbv_overflow_use, 1));

            if (q > q_limit) {
                if (s->avctx->debug & FF_DEBUG_RC)
                    printf(
                           "limiting QP %f -> %f\n", q, q_limit);
                q = q_limit;
            }
        }

        if (max_rate) {
            double d = 2 * expected_size / buffer_size;
            if (d > 1.0)
                d = 1.0;
            else if (d < 0.0001)
                d = 0.0001;
            q /= pow(d, 1.0 / s->avctx->rc_buffer_aggressivity);

            q_limit = bits2qp(rce,
                              FFMAX(rcc->buffer_index *
                                    s->avctx->rc_max_available_vbv_use,
                                    1));
            if (q < q_limit) {
                if (s->avctx->debug & FF_DEBUG_RC)
                    printf(
                           "limiting QP %f -> %f\n", q, q_limit);
                q = q_limit;
            }
        }
    }
//    av_dlog(s, "q:%f max:%f min:%f size:%f index:%f agr:%f\n",
//            q, max_rate, min_rate, buffer_size, rcc->buffer_index,
//            s->avctx->rc_buffer_aggressivity);
    if (s->avctx->rc_qsquish == 0.0 || qmin == qmax) {
        if (q < qmin)
            q = qmin;
        else if (q > qmax)
            q = qmax;
    } else {
        double min2 = log(qmin);
        double max2 = log(qmax);

        q  = log(q);
        q  = (q - min2) / (max2 - min2) - 0.5;
        q *= -4.0;
        q  = 1.0 / (1.0 + exp(q));
        q  = q * (max2 - min2) + min2;

        q = exp(q);
    }

    return q;
}



static void adaptive_quantization(MpegEncContext *s, double q)
{
    int i;
    const float lumi_masking         = s->avctx->lumi_masking / (128.0 * 128.0);
    const float dark_masking         = s->avctx->dark_masking / (128.0 * 128.0);
    const float temp_cplx_masking    = s->avctx->temporal_cplx_masking;
    const float spatial_cplx_masking = s->avctx->spatial_cplx_masking;
    const float p_masking            = s->avctx->p_masking;
    const float border_masking       = s->avctx->border_masking;
    float bits_sum                   = 0.0;
    float cplx_sum                   = 0.0;
    float *cplx_tab                  = s->cplx_tab;
    float *bits_tab                  = s->bits_tab;
    const int qmin                   = s->avctx->mb_lmin;
    const int qmax                   = s->avctx->mb_lmax;
    Picture *const pic               = &s->current_picture;
    const int mb_width               = s->mb_width;
    const int mb_height              = s->mb_height;

    for (i = 0; i < s->mb_num; i++) {
        const int mb_xy = s->mb_index2xy[i];
        float temp_cplx = sqrt(pic->mc_mb_var[mb_xy]); // FIXME merge in pow()
        float spat_cplx = sqrt(pic->mb_var[mb_xy]);
        const int lumi  = pic->mb_mean[mb_xy];
        float bits, cplx, factor;
        int mb_x = mb_xy % s->mb_stride;
        int mb_y = mb_xy / s->mb_stride;
        int mb_distance;
        float mb_factor = 0.0;
        if (spat_cplx < 4)
            spat_cplx = 4;              // FIXME finetune
        if (temp_cplx < 4)
            temp_cplx = 4;              // FIXME finetune

        if ((s->mb_type[mb_xy] & CANDIDATE_MB_TYPE_INTRA)) { // FIXME hq mode
            cplx   = spat_cplx;
            factor = 1.0 + p_masking;
        } else {
            cplx   = temp_cplx;
            factor = pow(temp_cplx, -temp_cplx_masking);
        }
        factor *= pow(spat_cplx, -spatial_cplx_masking);

        if (lumi > 127)
            factor *= (1.0 - (lumi - 128) * (lumi - 128) * lumi_masking);
        else
            factor *= (1.0 - (lumi - 128) * (lumi - 128) * dark_masking);

        if (mb_x < mb_width / 5) {
            mb_distance = mb_width / 5 - mb_x;
            mb_factor   = (float)mb_distance / (float)(mb_width / 5);
        } else if (mb_x > 4 * mb_width / 5) {
            mb_distance = mb_x - 4 * mb_width / 5;
            mb_factor   = (float)mb_distance / (float)(mb_width / 5);
        }
        if (mb_y < mb_height / 5) {
            mb_distance = mb_height / 5 - mb_y;
            mb_factor   = FFMAX(mb_factor,
                                (float)mb_distance / (float)(mb_height / 5));
        } else if (mb_y > 4 * mb_height / 5) {
            mb_distance = mb_y - 4 * mb_height / 5;
            mb_factor   = FFMAX(mb_factor,
                                (float)mb_distance / (float)(mb_height / 5));
        }

        factor *= 1.0 - border_masking * mb_factor;

        if (factor < 0.00001)
            factor = 0.00001;

        bits        = cplx * factor;
        cplx_sum   += cplx;
        bits_sum   += bits;
        cplx_tab[i] = cplx;
        bits_tab[i] = bits;
    }

    /* handle qmin/qmax clipping */
    if (s->flags & CODEC_FLAG_NORMALIZE_AQP) {
        float factor = bits_sum / cplx_sum;
        for (i = 0; i < s->mb_num; i++) {
            float newq = q * cplx_tab[i] / bits_tab[i];
            newq *= factor;

            if (newq > qmax) {
                bits_sum -= bits_tab[i];
                cplx_sum -= cplx_tab[i] * q / qmax;
            } else if (newq < qmin) {
                bits_sum -= bits_tab[i];
                cplx_sum -= cplx_tab[i] * q / qmin;
            }
        }
        if (bits_sum < 0.001)
            bits_sum = 0.001;
        if (cplx_sum < 0.001)
            cplx_sum = 0.001;
    }

    for (i = 0; i < s->mb_num; i++) {
        const int mb_xy = s->mb_index2xy[i];
        float newq      = q * cplx_tab[i] / bits_tab[i];
        int intq;

        if (s->flags & CODEC_FLAG_NORMALIZE_AQP) {
            newq *= bits_sum / cplx_sum;
        }

        intq = (int)(newq + 0.5);

        if (intq > qmax)
            intq = qmax;
        else if (intq < qmin)
            intq = qmin;
        s->lambda_table[mb_xy] = intq;
    }
}

float ff_rate_estimate_qscale(MpegEncContext *s, int dry_run)
{
	LOGD("ff_rate_estimate_qscale Called qscale:%d lambda:%d lambda2:%d\n",s->qscale,s->lambda,s->lambda2);
    float q = 0.0;
    int qmin, qmax;
    float br_compensation;
    double diff;
    double short_term_q;
    double fps;
    int picture_number = s->picture_number;
    int64_t wanted_bits;
    RateControlContext *rcc = &s->rc_context;
    AVCodecContext *a       = s->avctx;
    RateControlEntry local_rce = {0}, *rce;//local_rceが初期化されていなかった
    double bits;
    double rate_factor;
    int var;
    const int pict_type = s->pict_type;
    Picture * const pic = &s->current_picture;

    LOGD("TOTAL_BITS_:%d",s->total_bits);
#if CONFIG_LIBXVID
    if ((s->flags & CODEC_FLAG_PASS2) &&
        s->avctx->rc_strategy == FF_RC_STRATEGY_XVID)
        return ff_xvid_rate_estimate_qscale(s, dry_run);
#endif

    get_qminmax(&qmin, &qmax, s, pict_type);

    fps = get_fps(s->avctx);

    /* update predictors */
    if (picture_number > 2 && !dry_run) {
        const int last_var = s->last_pict_type == AV_PICTURE_TYPE_I ? rcc->last_mb_var_sum
                                                                    : rcc->last_mc_mb_var_sum;
//        av_assert1(s->frame_bits >= s->stuffing_bits);
        update_predictor(&rcc->pred[s->last_pict_type],
                         rcc->last_qscale,
                         sqrt(last_var),
                         s->frame_bits - s->stuffing_bits);
    }

    if (s->flags & CODEC_FLAG_PASS2) {
//        assert(picture_number >= 0);
        if (picture_number >= rcc->num_entries) {
            printf("Input is longer than 2-pass log file\n");
            return -1;
        }
        rce         = &rcc->entry[picture_number];
        wanted_bits = rce->expected_bits;
    } else {
    	LOGD("PASSED NO INIT local_rce:%d &local_rce:%d rce:%d\n",local_rce,&local_rce,rce);
        Picture *dts_pic;
        rce = &local_rce;//loca_rceは初期化されていないのでどっちもアドレスが同じという事をいいたい????

        /* FIXME add a dts field to AVFrame and ensure it is set and use it
         * here instead of reordering but the reordering is simpler for now
         * until H.264 B-pyramid must be handled. */
        if (s->pict_type == AV_PICTURE_TYPE_B || s->low_delay)
            dts_pic = s->current_picture_ptr;
        else
            dts_pic = s->last_picture_ptr;

        if (!dts_pic || dts_pic->f.pts == AV_NOPTS_VALUE)
            wanted_bits = (uint64_t)(s->bit_rate * (double)picture_number / fps);
        else
            wanted_bits = (uint64_t)(s->bit_rate * (double)dts_pic->f.pts / fps);
    }

    LOGD("ff_rate_estimate_qscale TOTAL_BITS:%d wanted_bits:%d",s->total_bits,wanted_bits);
    diff = s->total_bits - wanted_bits;
    br_compensation = (a->bit_rate_tolerance - diff) / a->bit_rate_tolerance;
    if (br_compensation <= 0.0)
        br_compensation = 0.001;

    LOGD("total_bits:%d wanted_bits:%d br_compensation:%f\n",s->total_bits,wanted_bits,br_compensation);
    //ここでmb_var_sumが違う
    LOGD("VAR IS mb_var_sum:%d mc_var_sum:%d\n",pic->mb_var_sum,pic->mc_mb_var_sum);
    var = pict_type == AV_PICTURE_TYPE_I ? pic->mb_var_sum : pic->mc_mb_var_sum;
    short_term_q = 0; /* avoid warning */
    if (s->flags & CODEC_FLAG_PASS2) {
        if (pict_type != AV_PICTURE_TYPE_I)
//            assert(pict_type == rce->new_pict_type);

        q = rce->new_qscale / br_compensation;
//        av_dlog(s, "%f %f %f last:%d var:%d type:%d//\n", q, rce->new_qscale,
//                br_compensation, s->frame_bits, var, pict_type);
    } else {
        rce->pict_type     =
        rce->new_pict_type = pict_type;
        rce->mc_mb_var_sum = pic->mc_mb_var_sum;
        rce->mb_var_sum    = pic->mb_var_sum;
        rce->qscale        = FF_QP2LAMBDA * 2;
        rce->f_code        = s->f_code;
        rce->b_code        = s->b_code;
        rce->misc_bits     = 1;

//        LOGD("predict_size before pict_type:%d rce_qscale:%d var:%e\n",pict_type,rce->qscale,var);

        bits = predict_size(&rcc->pred[pict_type], rce->qscale, sqrt(var));
    	LOGD("bits --------:%e\n",bits);
        if (pict_type == AV_PICTURE_TYPE_I) {
            rce->i_count    = s->mb_num;
            rce->i_tex_bits = bits;
            rce->p_tex_bits = 0;
            rce->mv_bits    = 0;
        } else {
            rce->i_count    = 0;    // FIXME we do know this approx
            rce->i_tex_bits = 0;
            rce->p_tex_bits = bits * 0.9;
            rce->mv_bits    = bits * 0.1;
        }
        rcc->i_cplx_sum[pict_type]  += rce->i_tex_bits * rce->qscale;
        rcc->p_cplx_sum[pict_type]  += rce->p_tex_bits * rce->qscale;
        rcc->mv_bits_sum[pict_type] += rce->mv_bits;
        rcc->frame_count[pict_type]++;


        bits        = rce->i_tex_bits + rce->p_tex_bits;
        rate_factor = rcc->pass1_wanted_bits /
                      rcc->pass1_rc_eq_output_sum * br_compensation;

        q = get_qscale(s, rce, rate_factor, picture_number);
        if (q < 0)
            return -1;

//        assert(q > 0.0);
        q = get_diff_limited_q(s, rce, q);
//        assert(q > 0.0);

        // FIXME type dependent blur like in 2-pass
        if (pict_type == AV_PICTURE_TYPE_P || s->intra_only) {
            rcc->short_term_qsum   *= a->qblur;
            rcc->short_term_qcount *= a->qblur;

            rcc->short_term_qsum += q;
            rcc->short_term_qcount++;
            q = short_term_q = rcc->short_term_qsum / rcc->short_term_qcount;
        }
//        assert(q > 0.0);

        q = modify_qscale(s, rce, q, picture_number);

        rcc->pass1_wanted_bits += s->bit_rate / fps;

//        assert(q > 0.0);
    }

    if (s->avctx->debug & FF_DEBUG_RC) {
//    	printf( "%c qp:%d<%2.1f<%d %d want:%d total:%d comp:%f st_q:%2.2f size:%d var:%d/%d br:%d fps:%d\n", av_get_picture_type_char(pict_type),
//               qmin, q, qmax, picture_number,
//               (int)wanted_bits / 1000, (int)s->total_bits / 1000,
//               br_compensation, short_term_q, s->frame_bits,
//               pic->mb_var_sum, pic->mc_mb_var_sum,
//               s->bit_rate / 1000, (int)fps);
    }

    if (q < qmin)
        q = qmin;
    else if (q > qmax)
        q = qmax;

    if (s->adaptive_quant)
        adaptive_quantization(s, q);
    else
        q = (int)(q + 0.5);

    if (!dry_run) {
        rcc->last_qscale        = q;
        rcc->last_mc_mb_var_sum = pic->mc_mb_var_sum;
        rcc->last_mb_var_sum    = pic->mb_var_sum;
    }
    LOGD("ff_rate_estimate_qscale E q:%f lambda:%d lambda2:%d\n",q,s->lambda,s->lambda2);

    return q;
}


/**
 * init s->current_picture.qscale_table from s->lambda_table
 */
void ff_init_qscale_tab(MpegEncContext *s)
{
    int8_t * const qscale_table = s->current_picture.f.qscale_table;
    int i;

    for (i = 0; i < s->mb_num; i++) {
        unsigned int lam = s->lambda_table[s->mb_index2xy[i]];
        int qp = (lam * 139 + FF_LAMBDA_SCALE * 64) >> (FF_LAMBDA_SHIFT + 7);
        qscale_table[s->mb_index2xy[i]] = av_clip_c(qp, s->avctx->qmin,
                                                  s->avctx->qmax);
    }
}

/**
 * modify qscale so that encoding is actually possible in h263 (limit difference to -2..2)
 */
void ff_clean_h263_qscales(MpegEncContext *s){
    int i;
    int8_t * const qscale_table = s->current_picture.f.qscale_table;

    ff_init_qscale_tab(s);

    for(i=1; i<s->mb_num; i++){
        if(qscale_table[ s->mb_index2xy[i] ] - qscale_table[ s->mb_index2xy[i-1] ] >2)
            qscale_table[ s->mb_index2xy[i] ]= qscale_table[ s->mb_index2xy[i-1] ]+2;
    }
    for(i=s->mb_num-2; i>=0; i--){
        if(qscale_table[ s->mb_index2xy[i] ] - qscale_table[ s->mb_index2xy[i+1] ] >2)
            qscale_table[ s->mb_index2xy[i] ]= qscale_table[ s->mb_index2xy[i+1] ]+2;
    }

    if(s->codec_id != AV_CODEC_ID_H263P){
        for(i=1; i<s->mb_num; i++){
            int mb_xy= s->mb_index2xy[i];

            if(qscale_table[mb_xy] != qscale_table[s->mb_index2xy[i-1]] && (s->mb_type[mb_xy]&CANDIDATE_MB_TYPE_INTER4V)){
                s->mb_type[mb_xy]|= CANDIDATE_MB_TYPE_INTER;
            }
        }
    }
}


/**
 * modify mb_type & qscale so that encoding is actually possible in mpeg4
 */
void ff_clean_mpeg4_qscales(MpegEncContext *s){
    int i;
    int8_t * const qscale_table = s->current_picture.f.qscale_table;

    ff_clean_h263_qscales(s);

    if(s->pict_type== AV_PICTURE_TYPE_B){
        int odd=0;
        /* ok, come on, this isn't funny anymore, there's more code for handling this mpeg4 mess than for the actual adaptive quantization */

        for(i=0; i<s->mb_num; i++){
            int mb_xy= s->mb_index2xy[i];
            odd += qscale_table[mb_xy]&1;
        }

        if(2*odd > s->mb_num) odd=1;
        else                  odd=0;

        for(i=0; i<s->mb_num; i++){
            int mb_xy= s->mb_index2xy[i];
            if((qscale_table[mb_xy]&1) != odd)
                qscale_table[mb_xy]++;
            if(qscale_table[mb_xy] > 31)
                qscale_table[mb_xy]= 31;
        }

        for(i=1; i<s->mb_num; i++){
            int mb_xy= s->mb_index2xy[i];
            if(qscale_table[mb_xy] != qscale_table[s->mb_index2xy[i-1]] && (s->mb_type[mb_xy]&CANDIDATE_MB_TYPE_DIRECT)){
                s->mb_type[mb_xy]|= CANDIDATE_MB_TYPE_BIDIR;
            }
        }
    }
}

static int estimate_qp(MpegEncContext *s, int dry_run){
	LOGD("estimate_qp Called dry_run:%d next_lambda:%d fixed_qscale:%d adaptive_q:%d\n",dry_run,s->next_lambda,s->fixed_qscale,s->adaptive_quant);
	LOGD("estimate_qp s->next_lambda:%d !s->fixed_qscale:%d\n",s->next_lambda,!s->fixed_qscale);

    if (s->next_lambda){
        s->current_picture_ptr->f.quality =
        s->current_picture.f.quality = s->next_lambda;
        if(!dry_run) s->next_lambda= 0;
    } else if (!s->fixed_qscale) {
        s->current_picture_ptr->f.quality =
        s->current_picture.f.quality = ff_rate_estimate_qscale(s, dry_run);
        if (s->current_picture.f.quality < 0)
            return -1;
    }

//    LOGD("s->current_picture.f.quality:%d\n",s->current_picture.f.quality);
    if(s->adaptive_quant){//毎回通ってない
        switch(s->codec_id){
        case AV_CODEC_ID_MPEG4:
            if (CONFIG_MPEG4_ENCODER)
                ff_clean_mpeg4_qscales(s);
            break;
        case AV_CODEC_ID_H263:
        case AV_CODEC_ID_H263P:
        case AV_CODEC_ID_FLV1:
            if (CONFIG_H263_ENCODER)
                ff_clean_h263_qscales(s);
            break;
        default:
            ff_init_qscale_tab(s);
        }

        s->lambda= s->lambda_table[0];
        //FIXME broken
    }else
        s->lambda = s->current_picture.f.quality;
    LOGD("estimate_qp update_q_Before qscale:%d s->lambda:%d s->lambda2:%d\n",s->qscale,s->lambda,s->lambda2);
    update_qscale(s);
    return 0;
}

/* must be called before writing the header */
static void set_frame_distances(MpegEncContext * s){
	LOGD("set_frame_distances Called\n");
//    assert(s->current_picture_ptr->f.pts != AV_NOPTS_VALUE);
    s->time = s->current_picture_ptr->f.pts * s->avctx->time_base.num;

    if(s->pict_type==AV_PICTURE_TYPE_B){
        s->pb_time= s->pp_time - (s->last_non_b_time - s->time);
//        assert(s->pb_time > 0 && s->pb_time < s->pp_time);
    }else{
        s->pp_time= s->time - s->last_non_b_time;
        s->last_non_b_time= s->time;
//        assert(s->picture_number==0 || s->pp_time > 0);
    }
}


//used by mpeg4 and rv10 decoder
void ff_mpeg4_init_direct_mv(MpegEncContext *s){
    int i;
    for(i=0; i<tab_size; i++){
        s->direct_scale_mv[0][i] = (i-tab_bias)*s->pb_time/s->pp_time;
        s->direct_scale_mv[1][i] = (i-tab_bias)*(s->pb_time-s->pp_time)/s->pp_time;
    }
}

/* must be called before writing the header */
void ff_set_mpeg4_time(MpegEncContext * s){
    if(s->pict_type==AV_PICTURE_TYPE_B){
        ff_mpeg4_init_direct_mv(s);
    }else{
        s->last_time_base= s->time_base;
        s->time_base= FFUDIV(s->time, s->avctx->time_base.den);
    }
}

void ff_get_2pass_fcode(MpegEncContext *s)
{
    RateControlContext *rcc = &s->rc_context;
    RateControlEntry *rce   = &rcc->entry[s->picture_number];

    s->f_code = rce->f_code;
    s->b_code = rce->b_code;
}

static void backup_duplicate_context(MpegEncContext *bak, MpegEncContext *src)
{
#define COPY(a) bak->a = src->a
    COPY(edge_emu_buffer);
    COPY(me.scratchpad);
    COPY(me.temp);
    COPY(rd_scratchpad);
    COPY(b_scratchpad);
    COPY(obmc_scratchpad);
    COPY(me.map);
    COPY(me.score_map);
    COPY(blocks);
    COPY(block);
    COPY(start_mb_y);
    COPY(end_mb_y);
    COPY(me.map_generation);
    COPY(pb);
    COPY(dct_error_sum);
    COPY(dct_count[0]);
    COPY(dct_count[1]);
    COPY(ac_val_base);
    COPY(ac_val[0]);
    COPY(ac_val[1]);
    COPY(ac_val[2]);
#undef COPY
}

int ff_update_duplicate_context(MpegEncContext *dst, MpegEncContext *src)
{
    MpegEncContext bak;
    int i, ret;
    // FIXME copy only needed parts
    // START_TIMER
    backup_duplicate_context(&bak, dst);
    memcpy(dst, src, sizeof(MpegEncContext));
    backup_duplicate_context(dst, &bak);
    for (i = 0; i < 12; i++) {
        dst->pblocks[i] = &dst->block[i];
    }
    if (!dst->edge_emu_buffer &&
        (ret = ff_mpv_frame_size_alloc(dst, dst->linesize)) < 0) {
        printf( "failed to allocate context "
               "scratch buffers.\n");
        return ret;
    }
    // STOP_TIMER("update_duplicate_context")
    // about 10k cycles / 0.01 sec for  1000frames on 1ghz with 2 threads
    return 0;
}


static int no_sub_motion_search(MpegEncContext * s,
          int *mx_ptr, int *my_ptr, int dmin,
                                  int src_index, int ref_index,
                                  int size, int h)
{
    (*mx_ptr)<<=1;
    (*my_ptr)<<=1;
    return dmin;
}

static int sad_hpel_motion_search(MpegEncContext * s,
                                  int *mx_ptr, int *my_ptr, int dmin,
                                  int src_index, int ref_index,
                                  int size, int h)
{
//	LOGD("sad_hpel_motion_search Called\n");
    MotionEstContext * const c= &s->me;
    const int penalty_factor= c->sub_penalty_factor;
    int mx, my, dminh;
    uint8_t *pix, *ptr;
    int stride= c->stride;
    const int flags= c->sub_flags;
    LOAD_COMMON

//    av_assert2(flags == 0);

    if(c->skip){
        *mx_ptr = 0;
        *my_ptr = 0;
        return dmin;
    }

    pix = c->src[src_index][0];

    mx = *mx_ptr;
    my = *my_ptr;
    ptr = c->ref[ref_index][0] + (my * stride) + mx;

    dminh = dmin;

    if (mx > xmin && mx < xmax &&
        my > ymin && my < ymax) {
        int dx=0, dy=0;
        int d, pen_x, pen_y;
        const int index= (my<<ME_MAP_SHIFT) + mx;
        const int t= score_map[(index-(1<<ME_MAP_SHIFT))&(ME_MAP_SIZE-1)];
        const int l= score_map[(index- 1               )&(ME_MAP_SIZE-1)];
        const int r= score_map[(index+ 1               )&(ME_MAP_SIZE-1)];
        const int b= score_map[(index+(1<<ME_MAP_SHIFT))&(ME_MAP_SIZE-1)];
        mx<<=1;
        my<<=1;


        pen_x= pred_x + mx;
        pen_y= pred_y + my;

        ptr-= stride;
        if(t<=b){
            CHECK_SAD_HALF_MV(y2 , 0, -1)
            if(l<=r){
                CHECK_SAD_HALF_MV(xy2, -1, -1)
                if(t+r<=b+l){
                    CHECK_SAD_HALF_MV(xy2, +1, -1)
                    ptr+= stride;
                }else{
                    ptr+= stride;
                    CHECK_SAD_HALF_MV(xy2, -1, +1)
                }
                CHECK_SAD_HALF_MV(x2 , -1,  0)
            }else{
                CHECK_SAD_HALF_MV(xy2, +1, -1)
                if(t+l<=b+r){
                    CHECK_SAD_HALF_MV(xy2, -1, -1)
                    ptr+= stride;
                }else{
                    ptr+= stride;
                    CHECK_SAD_HALF_MV(xy2, +1, +1)
                }
                CHECK_SAD_HALF_MV(x2 , +1,  0)
            }
        }else{
            if(l<=r){
                if(t+l<=b+r){
                    CHECK_SAD_HALF_MV(xy2, -1, -1)
                    ptr+= stride;
                }else{
                    ptr+= stride;
                    CHECK_SAD_HALF_MV(xy2, +1, +1)
                }
                CHECK_SAD_HALF_MV(x2 , -1,  0)
                CHECK_SAD_HALF_MV(xy2, -1, +1)
            }else{
                if(t+r<=b+l){
                    CHECK_SAD_HALF_MV(xy2, +1, -1)
                    ptr+= stride;
                }else{
                    ptr+= stride;
                    CHECK_SAD_HALF_MV(xy2, -1, +1)
                }
                CHECK_SAD_HALF_MV(x2 , +1,  0)
                CHECK_SAD_HALF_MV(xy2, +1, +1)
            }
            CHECK_SAD_HALF_MV(y2 ,  0, +1)
        }
        mx+=dx;
        my+=dy;

    }else{
        mx<<=1;
        my<<=1;
    }

    *mx_ptr = mx;
    *my_ptr = my;
    return dminh;
}

static void zero_hpel(uint8_t *a, const uint8_t *b, ptrdiff_t stride, int h){}

static int get_flags(MotionEstContext *c, int direct, int chroma){
    return   ((c->avctx->flags&CODEC_FLAG_QPEL) ? FLAG_QPEL : 0)
           + (direct ? FLAG_DIRECT : 0)
           + (chroma ? FLAG_CHROMA : 0);
}

int ff_init_me(MpegEncContext *s){
//	LOGD("motion_est ff_init_me Called\n");
    MotionEstContext * const c= &s->me;
    int cache_size= FFMIN(ME_MAP_SIZE>>ME_MAP_SHIFT, 1<<ME_MAP_SHIFT);
    int dia_size= FFMAX(FFABS(s->avctx->dia_size)&255, FFABS(s->avctx->pre_dia_size)&255);

    if(FFMIN(s->avctx->dia_size, s->avctx->pre_dia_size) < -FFMIN(ME_MAP_SIZE, MAX_SAB_SIZE)){
        printf( "ME_MAP size is too small for SAB diamond\n");
        return -1;
    }
    //special case of snow is needed because snow uses its own iterative ME code
    if(s->me_method!=ME_ZERO && s->me_method!=ME_EPZS && s->me_method!=ME_X1 && s->avctx->codec_id != AV_CODEC_ID_SNOW){
        printf( "me_method is only allowed to be set to zero and epzs; for hex,umh,full and others see dia_size\n");
        return -1;
    }

    c->avctx= s->avctx;

    if(cache_size < 2*dia_size && !c->stride){
        printf( "ME_MAP size may be a little small for the selected diamond size\n");
    }

    ff_set_cmp(&s->dsp, s->dsp.me_pre_cmp, c->avctx->me_pre_cmp);
    ff_set_cmp(&s->dsp, s->dsp.me_cmp, c->avctx->me_cmp);
    ff_set_cmp(&s->dsp, s->dsp.me_sub_cmp, c->avctx->me_sub_cmp);
    ff_set_cmp(&s->dsp, s->dsp.mb_cmp, c->avctx->mb_cmp);

    c->flags    = get_flags(c, 0, c->avctx->me_cmp    &FF_CMP_CHROMA);
    c->sub_flags= get_flags(c, 0, c->avctx->me_sub_cmp&FF_CMP_CHROMA);
    c->mb_flags = get_flags(c, 0, c->avctx->mb_cmp    &FF_CMP_CHROMA);

/*FIXME s->no_rounding b_type*/
    if(s->flags&CODEC_FLAG_QPEL){
        c->sub_motion_search= qpel_motion_search;
        c->qpel_avg= s->dsp.avg_qpel_pixels_tab;
        if(s->no_rounding) c->qpel_put= s->dsp.put_no_rnd_qpel_pixels_tab;
        else               c->qpel_put= s->dsp.put_qpel_pixels_tab;
    }else{
        if(c->avctx->me_sub_cmp&FF_CMP_CHROMA)
            c->sub_motion_search= hpel_motion_search;
        else if(   c->avctx->me_sub_cmp == FF_CMP_SAD
                && c->avctx->    me_cmp == FF_CMP_SAD
                && c->avctx->    mb_cmp == FF_CMP_SAD)
            c->sub_motion_search= sad_hpel_motion_search; // 2050 vs. 2450 cycles
        else
            c->sub_motion_search= hpel_motion_search;
    }
    c->hpel_avg= s->dsp.avg_pixels_tab;
    if(s->no_rounding) c->hpel_put= s->dsp.put_no_rnd_pixels_tab;
    else               c->hpel_put= s->dsp.put_pixels_tab;

    if(s->linesize){
        c->stride  = s->linesize;
        c->uvstride= s->uvlinesize;
    }else{
        c->stride  = 16*s->mb_width + 32;
        c->uvstride=  8*s->mb_width + 16;
    }

    /* 8x8 fullpel search would need a 4x4 chroma compare, which we do
     * not have yet, and even if we had, the motion estimation code
     * does not expect it. */
    if(s->codec_id != AV_CODEC_ID_SNOW){
        if((c->avctx->me_cmp&FF_CMP_CHROMA)/* && !s->dsp.me_cmp[2]*/){
            s->dsp.me_cmp[2]= 0;
        }
        if((c->avctx->me_sub_cmp&FF_CMP_CHROMA) && !s->dsp.me_sub_cmp[2]){
            s->dsp.me_sub_cmp[2]= 0;
        }
        c->hpel_put[2][0]= c->hpel_put[2][1]=
        c->hpel_put[2][2]= c->hpel_put[2][3]= zero_hpel;
    }

    if(s->codec_id == AV_CODEC_ID_H261){
        c->sub_motion_search= no_sub_motion_search;
    }

    return 0;
}


#define MERGE(field) dst->field += src->field; src->field=0
static void merge_context_after_me(MpegEncContext *dst, MpegEncContext *src){
    MERGE(me.scene_change_score);
    MERGE(me.mc_mb_var_sum_temp);
    MERGE(me.mb_var_sum_temp);
}

void ff_fix_long_p_mvs(MpegEncContext * s)
{
    MotionEstContext * const c= &s->me;
    const int f_code= s->f_code;
    int y, range;
//    av_assert0(s->pict_type==AV_PICTURE_TYPE_P);

    range = (((s->out_format == FMT_MPEG1 || s->msmpeg4_version) ? 8 : 16) << f_code);

//    av_assert0(range <= 16 || !s->msmpeg4_version);
//    av_assert0(range <=256 || !(s->codec_id == AV_CODEC_ID_MPEG2VIDEO && s->avctx->strict_std_compliance >= FF_COMPLIANCE_NORMAL));

    if(c->avctx->me_range && range > c->avctx->me_range) range= c->avctx->me_range;

    if(s->flags&CODEC_FLAG_4MV){
        const int wrap= s->b8_stride;

        /* clip / convert to intra 8x8 type MVs */
        for(y=0; y<s->mb_height; y++){
            int xy= y*2*wrap;
            int i= y*s->mb_stride;
            int x;

            for(x=0; x<s->mb_width; x++){
                if(s->mb_type[i]&CANDIDATE_MB_TYPE_INTER4V){
                    int block;
                    for(block=0; block<4; block++){
                        int off= (block& 1) + (block>>1)*wrap;
                        int mx = s->current_picture.f.motion_val[0][ xy + off ][0];
                        int my = s->current_picture.f.motion_val[0][ xy + off ][1];

                        if(   mx >=range || mx <-range
                           || my >=range || my <-range){
                            s->mb_type[i] &= ~CANDIDATE_MB_TYPE_INTER4V;
                            s->mb_type[i] |= CANDIDATE_MB_TYPE_INTRA;
                            s->current_picture.f.mb_type[i] = CANDIDATE_MB_TYPE_INTRA;
                        }
                    }
                }
                xy+=2;
                i++;
            }
        }
    }
}


/**
 *
 * @param truncate 1 for truncation, 0 for using intra
 */
void ff_fix_long_mvs(MpegEncContext * s, uint8_t *field_select_table, int field_select,
                     int16_t (*mv_table)[2], int f_code, int type, int truncate)
{
    MotionEstContext * const c= &s->me;
    int y, h_range, v_range;

    // RAL: 8 in MPEG-1, 16 in MPEG-4
    int range = (((s->out_format == FMT_MPEG1 || s->msmpeg4_version) ? 8 : 16) << f_code);

    if(c->avctx->me_range && range > c->avctx->me_range) range= c->avctx->me_range;

    h_range= range;
    v_range= field_select_table ? range>>1 : range;

    /* clip / convert to intra 16x16 type MVs */
    for(y=0; y<s->mb_height; y++){
        int x;
        int xy= y*s->mb_stride;
        for(x=0; x<s->mb_width; x++){
            if (s->mb_type[xy] & type){    // RAL: "type" test added...
                if(field_select_table==NULL || field_select_table[xy] == field_select){
                    if(   mv_table[xy][0] >=h_range || mv_table[xy][0] <-h_range
                       || mv_table[xy][1] >=v_range || mv_table[xy][1] <-v_range){

                        if(truncate){
                            if     (mv_table[xy][0] > h_range-1) mv_table[xy][0]=  h_range-1;
                            else if(mv_table[xy][0] < -h_range ) mv_table[xy][0]= -h_range;
                            if     (mv_table[xy][1] > v_range-1) mv_table[xy][1]=  v_range-1;
                            else if(mv_table[xy][1] < -v_range ) mv_table[xy][1]= -v_range;
                        }else{
                            s->mb_type[xy] &= ~type;
                            s->mb_type[xy] |= CANDIDATE_MB_TYPE_INTRA;
                            mv_table[xy][0]=
                            mv_table[xy][1]= 0;
                        }
                    }
                }
            }
            xy++;
        }
    }
}



static void find_best_tables(MpegEncContext * s)
{
    int i;
    int best        = 0, best_size        = INT_MAX;
    int chroma_best = 0, best_chroma_size = INT_MAX;

    for(i=0; i<3; i++){
        int level;
        int chroma_size=0;
        int size=0;

        if(i>0){// ;)
            size++;
            chroma_size++;
        }
        for(level=0; level<=MAX_LEVEL; level++){
            int run;
            for(run=0; run<=MAX_RUN; run++){
                int last;
                const int last_size= size + chroma_size;
                for(last=0; last<2; last++){
                    int inter_count       = s->ac_stats[0][0][level][run][last] + s->ac_stats[0][1][level][run][last];
                    int intra_luma_count  = s->ac_stats[1][0][level][run][last];
                    int intra_chroma_count= s->ac_stats[1][1][level][run][last];

                    if(s->pict_type==AV_PICTURE_TYPE_I){
                        size       += intra_luma_count  *rl_length[i  ][level][run][last];
                        chroma_size+= intra_chroma_count*rl_length[i+3][level][run][last];
                    }else{
                        size+=        intra_luma_count  *rl_length[i  ][level][run][last]
                                     +intra_chroma_count*rl_length[i+3][level][run][last]
                                     +inter_count       *rl_length[i+3][level][run][last];
                    }
                }
                if(last_size == size+chroma_size) break;
            }
        }
        if(size<best_size){
            best_size= size;
            best= i;
        }
        if(chroma_size<best_chroma_size){
            best_chroma_size= chroma_size;
            chroma_best= i;
        }
    }

    if(s->pict_type==AV_PICTURE_TYPE_P) chroma_best= best;

    memset(s->ac_stats, 0, sizeof(int)*(MAX_LEVEL+1)*(MAX_RUN+1)*2*2*2);

    s->rl_table_index       =        best;
    s->rl_chroma_table_index= chroma_best;

    if(s->pict_type != s->last_non_b_pict_type){
        s->rl_table_index= 2;
        if(s->pict_type==AV_PICTURE_TYPE_I)
            s->rl_chroma_table_index= 1;
        else
            s->rl_chroma_table_index= 2;
    }

}


void ff_msmpeg4_code012(PutBitContext *pb, int n)
{
    if (n == 0) {
        put_bits(pb, 1, 0);
    } else {
        put_bits(pb, 1, 1);
        put_bits(pb, 1, (n >= 2));
    }
}

/* write MSMPEG4 compatible frame header */
void ff_msmpeg4_encode_picture_header(MpegEncContext * s, int picture_number)
{
    find_best_tables(s);

    avpriv_align_put_bits(&s->pb);
    put_bits(&s->pb, 2, s->pict_type - 1);

    put_bits(&s->pb, 5, s->qscale);
    if(s->msmpeg4_version<=2){
        s->rl_table_index = 2;
        s->rl_chroma_table_index = 2;
    }

    s->dc_table_index = 1;
    s->mv_table_index = 1; /* only if P frame */
    s->use_skip_mb_code = 1; /* only if P frame */
    s->per_mb_rl_table = 0;
    if(s->msmpeg4_version==4)
        s->inter_intra_pred= (s->width*s->height < 320*240 && s->bit_rate<=II_BITRATE && s->pict_type==AV_PICTURE_TYPE_P);
    LOGD("pict_type:%d bit_rate:%d inter_intra_pred:%d width:%d height:%d\n", s->pict_type, s->bit_rate,s->inter_intra_pred, s->width, s->height);

    if (s->pict_type == AV_PICTURE_TYPE_I) {
        s->slice_height= s->mb_height/1;
        put_bits(&s->pb, 5, 0x16 + s->mb_height/s->slice_height);

        if(s->msmpeg4_version==4){
            ff_msmpeg4_encode_ext_header(s);
            if(s->bit_rate>MBAC_BITRATE)
                put_bits(&s->pb, 1, s->per_mb_rl_table);
        }

        if(s->msmpeg4_version>2){
            if(!s->per_mb_rl_table){
                ff_msmpeg4_code012(&s->pb, s->rl_chroma_table_index);
                ff_msmpeg4_code012(&s->pb, s->rl_table_index);
            }

            put_bits(&s->pb, 1, s->dc_table_index);
        }
    } else {
        put_bits(&s->pb, 1, s->use_skip_mb_code);

        if(s->msmpeg4_version==4 && s->bit_rate>MBAC_BITRATE)
            put_bits(&s->pb, 1, s->per_mb_rl_table);

        if(s->msmpeg4_version>2){
            if(!s->per_mb_rl_table)
                ff_msmpeg4_code012(&s->pb, s->rl_table_index);

            put_bits(&s->pb, 1, s->dc_table_index);

            put_bits(&s->pb, 1, s->mv_table_index);
        }
    }

    s->esc3_level_length= 0;
    s->esc3_run_length= 0;
}



static void mpeg4_encode_visual_object_header(MpegEncContext * s){
    int profile_and_level_indication;
    int vo_ver_id;

    if(s->avctx->profile != FF_PROFILE_UNKNOWN){
        profile_and_level_indication = s->avctx->profile << 4;
    }else if(s->max_b_frames || s->quarter_sample){
        profile_and_level_indication= 0xF0; // adv simple
    }else{
        profile_and_level_indication= 0x00; // simple
    }

    if(s->avctx->level != FF_LEVEL_UNKNOWN){
        profile_and_level_indication |= s->avctx->level;
    }else{
        profile_and_level_indication |= 1; //level 1
    }

    if(profile_and_level_indication>>4 == 0xF){
        vo_ver_id= 5;
    }else{
        vo_ver_id= 1;
    }

    //FIXME levels

    put_bits(&s->pb, 16, 0);
    put_bits(&s->pb, 16, VOS_STARTCODE);

    put_bits(&s->pb, 8, profile_and_level_indication);

    put_bits(&s->pb, 16, 0);
    put_bits(&s->pb, 16, VISUAL_OBJ_STARTCODE);

    put_bits(&s->pb, 1, 1);
        put_bits(&s->pb, 4, vo_ver_id);
        put_bits(&s->pb, 3, 1); //priority

    put_bits(&s->pb, 4, 1); //visual obj type== video obj

    put_bits(&s->pb, 1, 0); //video signal type == no clue //FIXME

    ff_mpeg4_stuffing(&s->pb);
}



void ff_write_quant_matrix(PutBitContext *pb, uint16_t *matrix)
{
    int i;

    if (matrix) {
        put_bits(pb, 1, 1);
        for (i = 0; i < 64; i++) {
            put_bits(pb, 8, matrix[ff_zigzag_direct[i]]);
        }
    } else
        put_bits(pb, 1, 0);
}


/**
 * Compare two rationals.
 * @param a first rational
 * @param b second rational
 * @return 0 if a==b, 1 if a>b, -1 if a<b, and INT_MIN if one of the
 * values is of the form 0/0
 */
static inline int av_cmp_q(AVRational a, AVRational b){
    const int64_t tmp= a.num * (int64_t)b.den - b.num * (int64_t)a.den;

    if(tmp) return ((tmp ^ a.den ^ b.den)>>63)|1;
    else if(b.den && a.den) return 0;
    else if(a.num && b.num) return (a.num>>31) - (b.num>>31);
    else                    return INT_MIN;
}

/**
 * Return the 4 bit value that specifies the given aspect ratio.
 * This may be one of the standard aspect ratios or it specifies
 * that the aspect will be stored explicitly later.
 */
int ff_h263_aspect_to_info(AVRational aspect){
    int i;

    if(aspect.num==0) aspect= (AVRational){1,1};

    for(i=1; i<6; i++){
        if(av_cmp_q(ff_h263_pixel_aspect[i], aspect) == 0){
            return i;
        }
    }

    return FF_ASPECT_EXTENDED;
}

void avpriv_put_string(PutBitContext *pb, const char *string, int terminate_string)
{
    while(*string){
        put_bits(pb, 8, *string);
        string++;
    }
    if(terminate_string)
        put_bits(pb, 8, 0);
}

static void mpeg4_encode_vol_header(MpegEncContext * s, int vo_number, int vol_number)
{
    int vo_ver_id;

    if (!CONFIG_MPEG4_ENCODER)  return;

    if(s->max_b_frames || s->quarter_sample){
        vo_ver_id= 5;
        s->vo_type= ADV_SIMPLE_VO_TYPE;
    }else{
        vo_ver_id= 1;
        s->vo_type= SIMPLE_VO_TYPE;
    }

    put_bits(&s->pb, 16, 0);
    put_bits(&s->pb, 16, 0x100 + vo_number);        /* video obj */
    put_bits(&s->pb, 16, 0);
    put_bits(&s->pb, 16, 0x120 + vol_number);       /* video obj layer */

    put_bits(&s->pb, 1, 0);             /* random access vol */
    put_bits(&s->pb, 8, s->vo_type);    /* video obj type indication */
    if(s->workaround_bugs & FF_BUG_MS) {
        put_bits(&s->pb, 1, 0);         /* is obj layer id= no */
    } else {
        put_bits(&s->pb, 1, 1);         /* is obj layer id= yes */
        put_bits(&s->pb, 4, vo_ver_id); /* is obj layer ver id */
        put_bits(&s->pb, 3, 1);         /* is obj layer priority */
    }

    s->aspect_ratio_info= ff_h263_aspect_to_info(s->avctx->sample_aspect_ratio);

    put_bits(&s->pb, 4, s->aspect_ratio_info);/* aspect ratio info */
    if (s->aspect_ratio_info == FF_ASPECT_EXTENDED){
        av_reduce(&s->avctx->sample_aspect_ratio.num, &s->avctx->sample_aspect_ratio.den,
                   s->avctx->sample_aspect_ratio.num,  s->avctx->sample_aspect_ratio.den, 255);
        put_bits(&s->pb, 8, s->avctx->sample_aspect_ratio.num);
        put_bits(&s->pb, 8, s->avctx->sample_aspect_ratio.den);
    }

    if(s->workaround_bugs & FF_BUG_MS) { //
        put_bits(&s->pb, 1, 0);         /* vol control parameters= no @@@ */
    } else {
        put_bits(&s->pb, 1, 1);         /* vol control parameters= yes */
        put_bits(&s->pb, 2, 1);         /* chroma format YUV 420/YV12 */
        put_bits(&s->pb, 1, s->low_delay);
        put_bits(&s->pb, 1, 0);         /* vbv parameters= no */
    }

    put_bits(&s->pb, 2, RECT_SHAPE);    /* vol shape= rectangle */
    put_bits(&s->pb, 1, 1);             /* marker bit */

    put_bits(&s->pb, 16, s->avctx->time_base.den);
    if (s->time_increment_bits < 1)
        s->time_increment_bits = 1;
    put_bits(&s->pb, 1, 1);             /* marker bit */
    put_bits(&s->pb, 1, 0);             /* fixed vop rate=no */
    put_bits(&s->pb, 1, 1);             /* marker bit */
    put_bits(&s->pb, 13, s->width);     /* vol width */
    put_bits(&s->pb, 1, 1);             /* marker bit */
    put_bits(&s->pb, 13, s->height);    /* vol height */
    put_bits(&s->pb, 1, 1);             /* marker bit */
    put_bits(&s->pb, 1, s->progressive_sequence ? 0 : 1);
    put_bits(&s->pb, 1, 1);             /* obmc disable */
    if (vo_ver_id == 1) {
        put_bits(&s->pb, 1, s->vol_sprite_usage);       /* sprite enable */
    }else{
        put_bits(&s->pb, 2, s->vol_sprite_usage);       /* sprite enable */
    }

    put_bits(&s->pb, 1, 0);             /* not 8 bit == false */
    put_bits(&s->pb, 1, s->mpeg_quant); /* quant type= (0=h263 style)*/

    if(s->mpeg_quant){
        ff_write_quant_matrix(&s->pb, s->avctx->intra_matrix);
        ff_write_quant_matrix(&s->pb, s->avctx->inter_matrix);
    }

    if (vo_ver_id != 1)
        put_bits(&s->pb, 1, s->quarter_sample);
    put_bits(&s->pb, 1, 1);             /* complexity estimation disable */
    s->resync_marker= s->rtp_mode;
    put_bits(&s->pb, 1, s->resync_marker ? 0 : 1);/* resync marker disable */
    put_bits(&s->pb, 1, s->data_partitioning ? 1 : 0);
    if(s->data_partitioning){
        put_bits(&s->pb, 1, 0);         /* no rvlc */
    }

    if (vo_ver_id != 1){
        put_bits(&s->pb, 1, 0);         /* newpred */
        put_bits(&s->pb, 1, 0);         /* reduced res vop */
    }
    put_bits(&s->pb, 1, 0);             /* scalability */

    ff_mpeg4_stuffing(&s->pb);

    /* user data */
    if(!(s->flags & CODEC_FLAG_BITEXACT)){
        put_bits(&s->pb, 16, 0);
        put_bits(&s->pb, 16, 0x1B2);    /* user_data */
        avpriv_put_string(&s->pb, LIBAVCODEC_IDENT, 0);
    }
}




static void mpeg4_encode_gop_header(MpegEncContext * s){
    int hours, minutes, seconds;
    int64_t time;

    put_bits(&s->pb, 16, 0);
    put_bits(&s->pb, 16, GOP_STARTCODE);

    time = s->current_picture_ptr->f.pts;
    if(s->reordered_input_picture[1])
        time = FFMIN(time, s->reordered_input_picture[1]->f.pts);
    time= time*s->avctx->time_base.num;
    s->last_time_base= FFUDIV(time, s->avctx->time_base.den);

    seconds= FFUDIV(time, s->avctx->time_base.den);
    minutes= FFUDIV(seconds, 60); seconds = FFUMOD(seconds, 60);
    hours  = FFUDIV(minutes, 60); minutes = FFUMOD(minutes, 60);
    hours  = FFUMOD(hours  , 24);

    put_bits(&s->pb, 5, hours);
    put_bits(&s->pb, 6, minutes);
    put_bits(&s->pb, 1, 1);
    put_bits(&s->pb, 6, seconds);

    put_bits(&s->pb, 1, !!(s->flags&CODEC_FLAG_CLOSED_GOP));
    put_bits(&s->pb, 1, 0); //broken link == NO

    ff_mpeg4_stuffing(&s->pb);
}

/* write mpeg4 VOP header */
void ff_mpeg4_encode_picture_header(MpegEncContext * s, int picture_number)
{
    int time_incr;
    int time_div, time_mod;

    if(s->pict_type==AV_PICTURE_TYPE_I){
        if(!(s->flags&CODEC_FLAG_GLOBAL_HEADER)){
            if(s->strict_std_compliance < FF_COMPLIANCE_VERY_STRICT) //HACK, the reference sw is buggy
                mpeg4_encode_visual_object_header(s);
            if(s->strict_std_compliance < FF_COMPLIANCE_VERY_STRICT || picture_number==0) //HACK, the reference sw is buggy
                mpeg4_encode_vol_header(s, 0, 0);
        }
        if(!(s->workaround_bugs & FF_BUG_MS))
            mpeg4_encode_gop_header(s);
    }

    s->partitioned_frame= s->data_partitioning && s->pict_type!=AV_PICTURE_TYPE_B;

    put_bits(&s->pb, 16, 0);                /* vop header */
    put_bits(&s->pb, 16, VOP_STARTCODE);    /* vop header */
    put_bits(&s->pb, 2, s->pict_type - 1);  /* pict type: I = 0 , P = 1 */

    time_div= FFUDIV(s->time, s->avctx->time_base.den);
    time_mod= FFUMOD(s->time, s->avctx->time_base.den);
    time_incr= time_div - s->last_time_base;
//    av_assert0(time_incr >= 0);
    while(time_incr--)
        put_bits(&s->pb, 1, 1);

    put_bits(&s->pb, 1, 0);

    put_bits(&s->pb, 1, 1);                             /* marker */
    put_bits(&s->pb, s->time_increment_bits, time_mod); /* time increment */
    put_bits(&s->pb, 1, 1);                             /* marker */
    put_bits(&s->pb, 1, 1);                             /* vop coded */
    if (    s->pict_type == AV_PICTURE_TYPE_P
        || (s->pict_type == AV_PICTURE_TYPE_S && s->vol_sprite_usage==GMC_SPRITE)) {
        put_bits(&s->pb, 1, s->no_rounding);    /* rounding type */
    }
    put_bits(&s->pb, 3, 0);     /* intra dc VLC threshold */
    if(!s->progressive_sequence){
         put_bits(&s->pb, 1, s->current_picture_ptr->f.top_field_first);
         put_bits(&s->pb, 1, s->alternate_scan);
    }
    //FIXME sprite stuff

    put_bits(&s->pb, 5, s->qscale);

    if (s->pict_type != AV_PICTURE_TYPE_I)
        put_bits(&s->pb, 3, s->f_code); /* fcode_for */
    if (s->pict_type == AV_PICTURE_TYPE_B)
        put_bits(&s->pb, 3, s->b_code); /* fcode_back */
}


void ff_rv10_encode_picture_header(MpegEncContext *s, int picture_number)
{
    int full_frame= 0;

    avpriv_align_put_bits(&s->pb);

    put_bits(&s->pb, 1, 1);     /* marker */

    put_bits(&s->pb, 1, (s->pict_type == AV_PICTURE_TYPE_P));

    put_bits(&s->pb, 1, 0);     /* not PB frame */

    put_bits(&s->pb, 5, s->qscale);

    if (s->pict_type == AV_PICTURE_TYPE_I) {
        /* specific MPEG like DC coding not used */
    }
    /* if multiple packets per frame are sent, the position at which
       to display the macroblocks is coded here */
    if(!full_frame){
        put_bits(&s->pb, 6, 0); /* mb_x */
        put_bits(&s->pb, 6, 0); /* mb_y */
        put_bits(&s->pb, 12, s->mb_width * s->mb_height);
    }

    put_bits(&s->pb, 3, 0);     /* ignored */
}


void ff_rv20_encode_picture_header(MpegEncContext *s, int picture_number){
    put_bits(&s->pb, 2, s->pict_type); //I 0 vs. 1 ?//ここでpict_typeが書き込まれる
    put_bits(&s->pb, 1, 0);     /* unknown bit */
    put_bits(&s->pb, 5, s->qscale);

    put_sbits(&s->pb, 8, picture_number); //FIXME wrong, but correct is not known
    s->mb_x= s->mb_y= 0;
    ff_h263_encode_mba(s);

    put_bits(&s->pb, 1, s->no_rounding);

//    av_assert0(s->f_code == 1);
//    av_assert0(s->unrestricted_mv == 0);
//    av_assert0(s->alt_inter_vlc == 0);
//    av_assert0(s->umvplus == 0);
//    av_assert0(s->modified_quant==1);
//    av_assert0(s->loop_filter==1);

    s->h263_aic= s->pict_type == AV_PICTURE_TYPE_I;
    if(s->h263_aic){
        s->y_dc_scale_table=
        s->c_dc_scale_table= ff_aic_dc_scale_table;
    }else{
        s->y_dc_scale_table=
        s->c_dc_scale_table= ff_mpeg1_dc_scale_table;
    }
}

void ff_flv_encode_picture_header(MpegEncContext * s, int picture_number)
{
//	  LOGD("ff_flv_encode_picture_header Called\n");
//	  LOGD("ff_flv_encode_picture_header picture_number:%d\n",picture_number);
      int format;

      avpriv_align_put_bits(&s->pb);

      put_bits(&s->pb, 17, 1);
      put_bits(&s->pb, 5, (s->h263_flv-1)); /* 0: h263 escape codes 1: 11-bit escape codes */
      put_bits(&s->pb, 8, (((int64_t)s->picture_number * 30 * s->avctx->time_base.num) / //FIXME use timestamp
                           s->avctx->time_base.den) & 0xff); /* TemporalReference */
      if (s->width == 352 && s->height == 288)
        format = 2;
      else if (s->width == 176 && s->height == 144)
        format = 3;
      else if (s->width == 128 && s->height == 96)
        format = 4;
      else if (s->width == 320 && s->height == 240)
        format = 5;
      else if (s->width == 160 && s->height == 120)
        format = 6;
      else if (s->width <= 255 && s->height <= 255)
        format = 0; /* use 1 byte width & height */
      else
        format = 1; /* use 2 bytes width & height */
      LOGD("ff_flv_encode_picture_header B\n");
      put_bits_(&s->pb, 3, format); /* PictureSize */
      if (format == 0) {
        put_bits(&s->pb, 8, s->width);
        put_bits(&s->pb, 8, s->height);
      } else if (format == 1) {
    	  LOGD("ff_flv_encode_picture_header format == 1\n");
    	  LOGD("ff_flv_encode_picture_header s width:%d height:%d\n",s->width,s->height);
        put_bits(&s->pb, 16, s->width);
        put_bits(&s->pb, 16, s->height);
      }
      put_bits(&s->pb, 2, s->pict_type == AV_PICTURE_TYPE_P); /* PictureType */
      put_bits(&s->pb, 1, 1); /* DeblockingFlag: on */
      put_bits(&s->pb, 5, s->qscale); /* Quantizer */
      put_bits(&s->pb, 1, 0); /* ExtraInformation */

      if(s->h263_aic){
        s->y_dc_scale_table=
          s->c_dc_scale_table= ff_aic_dc_scale_table;
      }else{
        s->y_dc_scale_table=
          s->c_dc_scale_table= ff_mpeg1_dc_scale_table;
      }

	  LOGD("ff_flv_encode_picture_header END\n");
}


int ff_match_2uint16(const uint16_t(*tab)[2], int size, int a, int b)
{
    int i;
    for (i = 0; i < size && !(tab[i][0] == a && tab[i][1] == b); i++) ;
    return i;
}

void ff_h263_encode_picture_header(MpegEncContext * s, int picture_number)
{
    int format, coded_frame_rate, coded_frame_rate_base, i, temp_ref;
    int best_clock_code=1;
    int best_divisor=60;
    int best_error= INT_MAX;

    if(s->h263_plus){
        for(i=0; i<2; i++){
            int div, error;
            div= (s->avctx->time_base.num*1800000LL + 500LL*s->avctx->time_base.den) / ((1000LL+i)*s->avctx->time_base.den);
            div= av_clip_c(div, 1, 127);
            error= FFABS(s->avctx->time_base.num*1800000LL - (1000LL+i)*s->avctx->time_base.den*div);
            if(error < best_error){
                best_error= error;
                best_divisor= div;
                best_clock_code= i;
            }
        }
    }
    s->custom_pcf= best_clock_code!=1 || best_divisor!=60;
    coded_frame_rate= 1800000;
    coded_frame_rate_base= (1000+best_clock_code)*best_divisor;

    avpriv_align_put_bits(&s->pb);

    /* Update the pointer to last GOB */
    s->ptr_lastgob = put_bits_ptr(&s->pb);
    put_bits(&s->pb, 22, 0x20); /* PSC */
    temp_ref= s->picture_number * (int64_t)coded_frame_rate * s->avctx->time_base.num / //FIXME use timestamp
                         (coded_frame_rate_base * (int64_t)s->avctx->time_base.den);
    put_sbits(&s->pb, 8, temp_ref); /* TemporalReference */

    put_bits(&s->pb, 1, 1);     /* marker */
    put_bits(&s->pb, 1, 0);     /* h263 id */
    put_bits(&s->pb, 1, 0);     /* split screen off */
    put_bits(&s->pb, 1, 0);     /* camera  off */
    put_bits(&s->pb, 1, 0);     /* freeze picture release off */

    format = ff_match_2uint16(ff_h263_format, FF_ARRAY_ELEMS(ff_h263_format), s->width, s->height);
    if (!s->h263_plus) {
        /* H.263v1 */
        put_bits(&s->pb, 3, format);
        put_bits(&s->pb, 1, (s->pict_type == AV_PICTURE_TYPE_P));
        /* By now UMV IS DISABLED ON H.263v1, since the restrictions
        of H.263v1 UMV implies to check the predicted MV after
        calculation of the current MB to see if we're on the limits */
        put_bits(&s->pb, 1, 0);         /* Unrestricted Motion Vector: off */
        put_bits(&s->pb, 1, 0);         /* SAC: off */
        put_bits(&s->pb, 1, s->obmc);   /* Advanced Prediction */
        put_bits(&s->pb, 1, 0);         /* only I/P frames, no PB frame */
        put_bits(&s->pb, 5, s->qscale);
        put_bits(&s->pb, 1, 0);         /* Continuous Presence Multipoint mode: off */
    } else {
        int ufep=1;
        /* H.263v2 */
        /* H.263 Plus PTYPE */

        put_bits(&s->pb, 3, 7);
        put_bits(&s->pb,3,ufep); /* Update Full Extended PTYPE */
        if (format == 8)
            put_bits(&s->pb,3,6); /* Custom Source Format */
        else
            put_bits(&s->pb, 3, format);

        put_bits(&s->pb,1, s->custom_pcf);
        put_bits(&s->pb,1, s->umvplus); /* Unrestricted Motion Vector */
        put_bits(&s->pb,1,0); /* SAC: off */
        put_bits(&s->pb,1,s->obmc); /* Advanced Prediction Mode */
        put_bits(&s->pb,1,s->h263_aic); /* Advanced Intra Coding */
        put_bits(&s->pb,1,s->loop_filter); /* Deblocking Filter */
        put_bits(&s->pb,1,s->h263_slice_structured); /* Slice Structured */
        put_bits(&s->pb,1,0); /* Reference Picture Selection: off */
        put_bits(&s->pb,1,0); /* Independent Segment Decoding: off */
        put_bits(&s->pb,1,s->alt_inter_vlc); /* Alternative Inter VLC */
        put_bits(&s->pb,1,s->modified_quant); /* Modified Quantization: */
        put_bits(&s->pb,1,1); /* "1" to prevent start code emulation */
        put_bits(&s->pb,3,0); /* Reserved */

        put_bits(&s->pb, 3, s->pict_type == AV_PICTURE_TYPE_P);

        put_bits(&s->pb,1,0); /* Reference Picture Resampling: off */
        put_bits(&s->pb,1,0); /* Reduced-Resolution Update: off */
        put_bits(&s->pb,1,s->no_rounding); /* Rounding Type */
        put_bits(&s->pb,2,0); /* Reserved */
        put_bits(&s->pb,1,1); /* "1" to prevent start code emulation */

        /* This should be here if PLUSPTYPE */
        put_bits(&s->pb, 1, 0); /* Continuous Presence Multipoint mode: off */

        if (format == 8) {
            /* Custom Picture Format (CPFMT) */
            s->aspect_ratio_info= ff_h263_aspect_to_info(s->avctx->sample_aspect_ratio);

            put_bits(&s->pb,4,s->aspect_ratio_info);
            put_bits(&s->pb,9,(s->width >> 2) - 1);
            put_bits(&s->pb,1,1); /* "1" to prevent start code emulation */
            put_bits(&s->pb,9,(s->height >> 2));
            if (s->aspect_ratio_info == FF_ASPECT_EXTENDED){
                put_bits(&s->pb, 8, s->avctx->sample_aspect_ratio.num);
                put_bits(&s->pb, 8, s->avctx->sample_aspect_ratio.den);
            }
        }
        if(s->custom_pcf){
            if(ufep){
                put_bits(&s->pb, 1, best_clock_code);
                put_bits(&s->pb, 7, best_divisor);
            }
            put_sbits(&s->pb, 2, temp_ref>>8);
        }

        /* Unlimited Unrestricted Motion Vectors Indicator (UUI) */
        if (s->umvplus)
//            put_bits(&s->pb,1,1); /* Limited according tables of Annex D */
//FIXME check actual requested range
            put_bits(&s->pb,2,1); /* unlimited */
        if(s->h263_slice_structured)
            put_bits(&s->pb,2,0); /* no weird submodes */

        put_bits(&s->pb, 5, s->qscale);
    }

    put_bits(&s->pb, 1, 0);     /* no PEI */

    if(s->h263_slice_structured){
        put_bits(&s->pb, 1, 1);

//        av_assert1(s->mb_x == 0 && s->mb_y == 0);
        ff_h263_encode_mba(s);

        put_bits(&s->pb, 1, 1);
    }

    if(s->h263_aic){
         s->y_dc_scale_table=
         s->c_dc_scale_table= ff_aic_dc_scale_table;
    }else{
        s->y_dc_scale_table=
        s->c_dc_scale_table= ff_mpeg1_dc_scale_table;
    }
}

static void update_duplicate_context_after_me(MpegEncContext *dst,
                                              MpegEncContext *src)
{
#define COPY(a) dst->a= src->a
    COPY(pict_type);
    COPY(current_picture);
    COPY(f_code);
    COPY(b_code);
    COPY(qscale);
    COPY(lambda);
    COPY(lambda2);
    COPY(picture_in_gop_number);
    COPY(gop_picture_number);
    COPY(frame_pred_frame_dct); // FIXME don't set in encode_header
    COPY(progressive_frame);    // FIXME don't set in encode_header
    COPY(partitioned_frame);    // FIXME don't set in encode_header
#undef COPY
}


static void merge_context_after_encode(MpegEncContext *dst, MpegEncContext *src){
    int i;

    MERGE(dct_count[0]); //note, the other dct vars are not part of the context
    MERGE(dct_count[1]);
    MERGE(mv_bits);
    MERGE(i_tex_bits);
    MERGE(p_tex_bits);
    MERGE(i_count);
    MERGE(f_count);
    MERGE(b_count);
    MERGE(skip_count);
    MERGE(misc_bits);
    MERGE(er.error_count);
    MERGE(padding_bug_score);
    MERGE(current_picture.f.error[0]);
    MERGE(current_picture.f.error[1]);
    MERGE(current_picture.f.error[2]);

    if(dst->avctx->noise_reduction){
        for(i=0; i<64; i++){
            MERGE(dct_error_sum[0][i]);
            MERGE(dct_error_sum[1][i]);
        }
    }

//    assert(put_bits_count(&src->pb) % 8 ==0);
//    assert(put_bits_count(&dst->pb) % 8 ==0);
    avpriv_copy_bits(&dst->pb, src->pb.buf, put_bits_count(&src->pb));
    flush_put_bits(&dst->pb);
}

static int encode_picture(MpegEncContext *s, int picture_number)
{
//    LOGD("encode_picture Called\n");
    //	LOGD("encode_picture A current_ptr->mb_var_sum:%d\n",s->current_picture_ptr->mb_var_sum);

    int i, ret;
    int bits;
    int context_count = s->slice_context_count;

    s->picture_number = picture_number;

    /* Reset the average MB variance */
    s->me.mb_var_sum_temp    =
    s->me.mc_mb_var_sum_temp = 0;

    /* we need to initialize some time vars before we can encode b-frames */
    // RAL: Condition added for MPEG1VIDEO
    if (s->codec_id == AV_CODEC_ID_MPEG1VIDEO || s->codec_id == AV_CODEC_ID_MPEG2VIDEO || (s->h263_pred && !s->msmpeg4_version))
        set_frame_distances(s);
    if(CONFIG_MPEG4_ENCODER && s->codec_id == AV_CODEC_ID_MPEG4)
        ff_set_mpeg4_time(s);

    s->me.scene_change_score=0;

//    s->lambda= s->current_picture_ptr->quality; //FIXME qscale / ... stuff for ME rate distortion

    if(s->pict_type==AV_PICTURE_TYPE_I){
        if(s->msmpeg4_version >= 3) s->no_rounding=1;
        else                        s->no_rounding=0;
    }else if(s->pict_type!=AV_PICTURE_TYPE_B){
        if(s->flipflop_rounding || s->codec_id == AV_CODEC_ID_H263P || s->codec_id == AV_CODEC_ID_MPEG4)
            s->no_rounding ^= 1;
    }

    if(s->flags & CODEC_FLAG_PASS2){
        if (estimate_qp(s,1) < 0)
            return -1;
        ff_get_2pass_fcode(s);
    }else if(!(s->flags & CODEC_FLAG_QSCALE)){
        if(s->pict_type==AV_PICTURE_TYPE_B)
            s->lambda= s->last_lambda_for[s->pict_type];
        else
            s->lambda= s->last_lambda_for[s->last_non_b_pict_type];
//        LOGD("encode_picture update_q_Before\n");
        update_qscale(s);
    }

    if(s->codec_id != AV_CODEC_ID_AMV){
        if(s->q_chroma_intra_matrix   != s->q_intra_matrix  ) av_freep(&s->q_chroma_intra_matrix);
        if(s->q_chroma_intra_matrix16 != s->q_intra_matrix16) av_freep(&s->q_chroma_intra_matrix16);
        s->q_chroma_intra_matrix   = s->q_intra_matrix;
        s->q_chroma_intra_matrix16 = s->q_intra_matrix16;
    }

    s->mb_intra=0; //for the rate distortion & bit compare functions
    for(i=1; i<context_count; i++){
        ret = ff_update_duplicate_context(s->thread_context[i], s);
        if (ret < 0)
            return ret;
    }

    if(&s->me){
    	MotionEstContext moo = (MotionEstContext)s->me;
    }

    if(ff_init_me(s)<0)
        return -1;

    if(&s->me){
        	MotionEstContext moo = (MotionEstContext)s->me;
        }

//    LOGD("encode_picture s->mb_stride:%d s->mb_height:%d\n",s->mb_stride,s->mb_height);
//    LOGD("encode_picture !s->fixed_qscale:%d\n",!s->fixed_qscale);
    /* Estimate motion for every MB */
    if(s->pict_type != AV_PICTURE_TYPE_I){
        s->lambda = (s->lambda * s->avctx->me_penalty_compensation + 128)>>8;
        s->lambda2= (s->lambda2* (int64_t)s->avctx->me_penalty_compensation + 128)>>8;
        if(s->pict_type != AV_PICTURE_TYPE_B && s->avctx->me_threshold==0){
            if((s->avctx->pre_me && s->last_non_b_pict_type==AV_PICTURE_TYPE_I) || s->avctx->pre_me==2){
                LOGD("encode_picture I-FRAME\n");
                LOGD("encode_picture execute Before1\n");
                s->avctx->execute(s->avctx, pre_estimate_motion_thread, &s->thread_context[0], NULL, context_count, sizeof(void*));
            }
        }

        s->avctx->execute(s->avctx, estimate_motion_thread, &s->thread_context[0], NULL, context_count, sizeof(void*));
    }else /* if(s->pict_type == AV_PICTURE_TYPE_I) */{
        /* I-Frame */
        for(i=0; i<s->mb_stride*s->mb_height; i++)
            s->mb_type[i]= CANDIDATE_MB_TYPE_INTRA;

        if(&s->me){
        	MotionEstContext moo = (MotionEstContext)s->me;
        }
        if(!s->fixed_qscale){//ここでexecuteに渡すときにDSPが無くなっている
            /* finding spatial complexity for I-frame rate control */
            s->avctx->execute(s->avctx, mb_var_thread, &s->thread_context[0], NULL, context_count, sizeof(void*));
            if(&s->me){
            	MotionEstContext moo = (MotionEstContext)s->me;
            }
        }
    }

//    LOGD("encode_picture U pbc:%d\n",put_bits_count(&(s->pb)));
//        LOGD( "encode_thread OUT TEST AA size:%d\n",s->avctx->internal->byte_buffer_size);
//        if(s->avctx->internal->byte_buffer_size){
//                   for(i = 0; i < 100 && i< s->avctx->internal->byte_buffer_size; i++){
//                	   LOGD( "%02X ",s->avctx->internal->byte_buffer[i]);
//                   }
//                   LOGD("\n");
//               }

    for(i=1; i<context_count; i++){
        merge_context_after_me(s, s->thread_context[i]);
    }
    //ここでme->mb_var_sum_tempがcurrent_picture->mb_var_sumに代入されている
    //元々current_picture_ptrはselect_input_pictureでセットされる
//    LOGD("encode_picture Y2 s->me.   mb_var_sum_temp:%d\n",s->me.   mb_var_sum_temp);
//    LOGD("encode_picture Y2 current->mb_var_sum:%d ptr->var_sum:%d\n",s->current_picture.mb_var_sum,s->current_picture_ptr->mb_var_sum);
    s->current_picture.mc_mb_var_sum= s->current_picture_ptr->mc_mb_var_sum= s->me.mc_mb_var_sum_temp;
    s->current_picture.   mb_var_sum= s->current_picture_ptr->   mb_var_sum= s->me.   mb_var_sum_temp;
//    LOGD("encode_picture A1 current_ptr->mb_var_sum:%d current->mb_var_sum:%d\n",s->current_picture_ptr->mb_var_sum,s->current_picture.mb_var_sum);



    if(s->me.scene_change_score > s->avctx->scenechange_threshold && s->pict_type == AV_PICTURE_TYPE_P){
        s->pict_type= AV_PICTURE_TYPE_I;
        for(i=0; i<s->mb_stride*s->mb_height; i++)
            s->mb_type[i]= CANDIDATE_MB_TYPE_INTRA;
        if(s->msmpeg4_version >= 3)
            s->no_rounding=1;
//        av_dlog(s, "Scene change detected, encoding as I Frame %d %d\n",
//                s->current_picture.mb_var_sum, s->current_picture.mc_mb_var_sum);
    }

    if(!s->umvplus){
        if(s->pict_type==AV_PICTURE_TYPE_P || s->pict_type==AV_PICTURE_TYPE_S) {
            s->f_code= ff_get_best_fcode(s, s->p_mv_table, CANDIDATE_MB_TYPE_INTER);

            if(s->flags & CODEC_FLAG_INTERLACED_ME){
                int a,b;
                a= ff_get_best_fcode(s, s->p_field_mv_table[0][0], CANDIDATE_MB_TYPE_INTER_I); //FIXME field_select
                b= ff_get_best_fcode(s, s->p_field_mv_table[1][1], CANDIDATE_MB_TYPE_INTER_I);
                s->f_code= FFMAX3(s->f_code, a, b);
            }

            ff_fix_long_p_mvs(s);
            ff_fix_long_mvs(s, NULL, 0, s->p_mv_table, s->f_code, CANDIDATE_MB_TYPE_INTER, 0);
            if(s->flags & CODEC_FLAG_INTERLACED_ME){
                int j;
                for(i=0; i<2; i++){
                    for(j=0; j<2; j++)
                        ff_fix_long_mvs(s, s->p_field_select_table[i], j,
                                        s->p_field_mv_table[i][j], s->f_code, CANDIDATE_MB_TYPE_INTER_I, 0);
                }
            }
        }

        if(s->pict_type==AV_PICTURE_TYPE_B){
//            LOGD("encode_picture B-FRAME\n");
            int a, b;

            a = ff_get_best_fcode(s, s->b_forw_mv_table, CANDIDATE_MB_TYPE_FORWARD);
            b = ff_get_best_fcode(s, s->b_bidir_forw_mv_table, CANDIDATE_MB_TYPE_BIDIR);
            s->f_code = FFMAX(a, b);

            a = ff_get_best_fcode(s, s->b_back_mv_table, CANDIDATE_MB_TYPE_BACKWARD);
            b = ff_get_best_fcode(s, s->b_bidir_back_mv_table, CANDIDATE_MB_TYPE_BIDIR);
            s->b_code = FFMAX(a, b);

            ff_fix_long_mvs(s, NULL, 0, s->b_forw_mv_table, s->f_code, CANDIDATE_MB_TYPE_FORWARD, 1);
            ff_fix_long_mvs(s, NULL, 0, s->b_back_mv_table, s->b_code, CANDIDATE_MB_TYPE_BACKWARD, 1);
            ff_fix_long_mvs(s, NULL, 0, s->b_bidir_forw_mv_table, s->f_code, CANDIDATE_MB_TYPE_BIDIR, 1);
            ff_fix_long_mvs(s, NULL, 0, s->b_bidir_back_mv_table, s->b_code, CANDIDATE_MB_TYPE_BIDIR, 1);
            if(s->flags & CODEC_FLAG_INTERLACED_ME){
                int dir, j;
                for(dir=0; dir<2; dir++){
                    for(i=0; i<2; i++){
                        for(j=0; j<2; j++){
                            int type= dir ? (CANDIDATE_MB_TYPE_BACKWARD_I|CANDIDATE_MB_TYPE_BIDIR_I)
                                          : (CANDIDATE_MB_TYPE_FORWARD_I |CANDIDATE_MB_TYPE_BIDIR_I);
                            ff_fix_long_mvs(s, s->b_field_select_table[dir][i], j,
                                            s->b_field_mv_table[dir][i][j], dir ? s->b_code : s->f_code, type, 1);
                        }
                    }
                }
            }
        }
    }

//    LOGD("encode_picture estimate_qp before current->mb_var_sum:%d\n",s->current_picture.mb_var_sum);
    if (estimate_qp(s, 0) < 0)
        return -1;

    if(s->qscale < 3 && s->max_qcoeff<=128 && s->pict_type==AV_PICTURE_TYPE_I && !(s->flags & CODEC_FLAG_QSCALE))
        s->qscale= 3; //reduce clipping problems

    if (s->out_format == FMT_MJPEG) {
        /* for mjpeg, we do include qscale in the matrix */
        for(i=1;i<64;i++){
            int j= s->dsp.idct_permutation[i];

            s->intra_matrix[j] = av_clip_uint8_c((ff_mpeg1_default_intra_matrix[i] * s->qscale) >> 3);
        }
        s->y_dc_scale_table=
        s->c_dc_scale_table= ff_mpeg2_dc_scale_table[s->intra_dc_precision];
        s->intra_matrix[0] = ff_mpeg2_dc_scale_table[s->intra_dc_precision][8];
        ff_convert_matrix(&s->dsp, s->q_intra_matrix, s->q_intra_matrix16,
                       s->intra_matrix, s->intra_quant_bias, 8, 8, 1);
        s->qscale= 8;
    }
    if(s->codec_id == AV_CODEC_ID_AMV){
        static const uint8_t y[32]={13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13};
        static const uint8_t c[32]={14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14};
        for(i=1;i<64;i++){
            int j= s->dsp.idct_permutation[ff_zigzag_direct[i]];

            s->intra_matrix[j] = sp5x_quant_table[5*2+0][i];
            s->chroma_intra_matrix[j] = sp5x_quant_table[5*2+1][i];
        }
        s->y_dc_scale_table= y;
        s->c_dc_scale_table= c;
        s->intra_matrix[0] = 13;
        s->chroma_intra_matrix[0] = 14;
        ff_convert_matrix(&s->dsp, s->q_intra_matrix, s->q_intra_matrix16,
                       s->intra_matrix, s->intra_quant_bias, 8, 8, 1);
        ff_convert_matrix(&s->dsp, s->q_chroma_intra_matrix, s->q_chroma_intra_matrix16,
                       s->chroma_intra_matrix, s->intra_quant_bias, 8, 8, 1);
        s->qscale= 8;
    }

    //FIXME var duplication
    s->current_picture_ptr->f.key_frame =
    s->current_picture.f.key_frame = s->pict_type == AV_PICTURE_TYPE_I; //FIXME pic_ptr
    s->current_picture_ptr->f.pict_type =
    s->current_picture.f.pict_type = s->pict_type;

//    LOGD( "encode_thread OUT TESTB byte_buffer_size:%d\n",s->avctx->internal->byte_buffer_size);
//    if(s->avctx->internal->byte_buffer_size){
//               for(i = 0; i < 100 && i< s->avctx->internal->byte_buffer_size; i++){
//            	   LOGD( "%02X ",s->avctx->internal->byte_buffer[i]);
//               }
//               LOGD("\n");
//           }
   if (s->current_picture.f.key_frame)
        s->picture_in_gop_number=0;

    s->mb_x = s->mb_y = 0;
    s->last_bits= put_bits_count(&s->pb);
            ff_flv_encode_picture_header(s, picture_number);
    //ここでフレームヘッダー?はできている(初回74バイトを確認)
//    LOGD( "encode_thread OUT TESTAA byte_buffer_size:%d\n",s->avctx->internal->byte_buffer_size);
//    if(s->avctx->internal->byte_buffer_size){
//               for(i = 0; i < 100 && i< s->avctx->internal->byte_buffer_size; i++){
//            	   LOGD( "%02X ",s->avctx->internal->byte_buffer[i]);
//               }
//               LOGD("\n");
//           }

    bits= put_bits_count(&s->pb);
    s->header_bits= bits - s->last_bits;
    LOGD("bits_count bits:%d",bits);
    for(i=1; i<context_count; i++){
        update_duplicate_context_after_me(s->thread_context[i], s);
    }
    //初回この関数のちょい上でフレームヘッダー?がエンコードされ、このexecuteでデータがエンコードされる
	s->avctx->execute(s->avctx, encode_thread, &s->thread_context[0], NULL, context_count, sizeof(void*));
    for(i=1; i<context_count; i++){
        merge_context_after_encode(s, s->thread_context[i]);
    }
    //ここでデータができている
    LOGD( "encode_picture END avctx->internal->byte_buffer_size:%d\n",s->avctx->internal->byte_buffer_size);
    return 0;
}

int av_packet_shrink_side_data(AVPacket *pkt, enum AVPacketSideDataType type,
                               int size)
{
    int i;

    for (i = 0; i < pkt->side_data_elems; i++) {
        if (pkt->side_data[i].type == type) {
            if (size > pkt->side_data[i].size)
                return -1;
            pkt->side_data[i].size = size;
            return 0;
        }
    }
    return -1;
}

void ff_write_pass1_stats(MpegEncContext *s)
{
    LOGD(s->avctx->stats_out, 256,
             "in:%d out:%d type:%d q:%d itex:%d ptex:%d mv:%d misc:%d "
             "fcode:%d bcode:%d mc-var:%d var:%d icount:%d skipcount:%d hbits:%d;\n",
             s->current_picture_ptr->f.display_picture_number,
             s->current_picture_ptr->f.coded_picture_number,
             s->pict_type,
             s->current_picture.f.quality,
             s->i_tex_bits,
             s->p_tex_bits,
             s->mv_bits,
             s->misc_bits,
             s->f_code,
             s->b_code,
             s->current_picture.mc_mb_var_sum,
             s->current_picture.mb_var_sum,
             s->i_count, s->skip_count,
             s->header_bits);
}


int sws_getColorspaceDetails(struct SwsContext *c, int **inv_table,
                             int *srcRange, int **table, int *dstRange,
                             int *brightness, int *contrast, int *saturation)
{
	LOGD("sws_getColorspaceDetails Called\n");
    if (!c )
        return -1;

    *inv_table  = c->srcColorspaceTable;
    *table      = c->dstColorspaceTable;
    *srcRange   = c->srcRange;
    *dstRange   = c->dstRange;
    *brightness = c->brightness;
    *contrast   = c->contrast;
    *saturation = c->saturation;

    return 0;
}



#define SWS_CS_DEFAULT        5
const int32_t ff_yuv2rgb_coeffs[8][4] = {
    { 117504, 138453, 13954, 34903 }, /* no sequence_display_extension */
    { 117504, 138453, 13954, 34903 }, /* ITU-R Rec. 709 (1990) */
    { 104597, 132201, 25675, 53279 }, /* unspecified */
    { 104597, 132201, 25675, 53279 }, /* reserved */
    { 104448, 132798, 24759, 53109 }, /* FCC */
    { 104597, 132201, 25675, 53279 }, /* ITU-R Rec. 624-4 System B, G */
    { 104597, 132201, 25675, 53279 }, /* SMPTE 170M */
    { 117579, 136230, 16907, 35559 }  /* SMPTE 240M (1987) */
};
const int *sws_getCoefficients(int colorspace)
{
    if (colorspace > 7 || colorspace < 0)
        colorspace = SWS_CS_DEFAULT;
    return ff_yuv2rgb_coeffs[colorspace];
}

static const int *parse_yuv_type(const char *s, enum AVColorSpace colorspace)
{
//	LOGD("parse_yuv_type s:%s",s);
    if (!s)
        s = "bt601";

    if (s && strstr(s, "bt709")) {
        colorspace = AVCOL_SPC_BT709;
    } else if (s && strstr(s, "fcc")) {
        colorspace = AVCOL_SPC_FCC;
    } else if (s && strstr(s, "smpte240m")) {
        colorspace = AVCOL_SPC_SMPTE240M;
    } else if (s && (strstr(s, "bt601") || strstr(s, "bt470") || strstr(s, "smpte170m"))) {
        colorspace = AVCOL_SPC_BT470BG;
    }

    if (colorspace < 1 || colorspace > 7) {
        colorspace = AVCOL_SPC_BT470BG;
    }

    return sws_getCoefficients(colorspace);
}

#   define AV_PIX_FMT_NE(be, le) AV_PIX_FMT_##le

#define AV_PIX_FMT_RGB32   AV_PIX_FMT_NE(ARGB, BGRA)
#define AV_PIX_FMT_RGB32_1 AV_PIX_FMT_NE(RGBA, ABGR)
#define AV_PIX_FMT_BGR32_1 AV_PIX_FMT_NE(BGRA, ARGB)

#define CONFIG_SWSCALE_ALPHA 1
#    define av_uninit(x) x=x


/**
 * Clip a signed integer value into the 0-255 range.
 * @param a value to clip
 * @return clipped value
 */
inline const uint8_t av_clip_uint8_c(int a)
{
    if (a&(~0xFF)) return (-a)>>31;
    else           return a;
}


static uint16_t roundToInt16(int64_t f)
{
    int r = (f + (1 << 15)) >> 16;

    if (r < -0x7FFF)
        return 0x8000;
    else if (r > 0x7FFF)
        return 0x7FFF;
    else
        return r;
}

static void fill_table(uint8_t* table[256 + 2*YUVRGB_TABLE_HEADROOM], const int elemsize,
                       const int64_t inc, void *y_tab)
{
    int i;
    uint8_t *y_table = y_tab;

    y_table -= elemsize * (inc >> 9);

    for (i = 0; i < 256 + 2*YUVRGB_TABLE_HEADROOM; i++) {
        int64_t cb = av_clip_c(i-YUVRGB_TABLE_HEADROOM, 0, 255)*inc;
        table[i] = y_table + elemsize * (cb >> 16);
    }
}

static void fill_gv_table(int table[256 + 2*YUVRGB_TABLE_HEADROOM], const int elemsize, const int64_t inc)
{
    int i;
    int off    = -(inc >> 9);

    for (i = 0; i < 256 + 2*YUVRGB_TABLE_HEADROOM; i++) {
        int64_t cb = av_clip_c(i-YUVRGB_TABLE_HEADROOM, 0, 255)*inc;
        table[i] = elemsize * (off + (cb >> 16));
    }
}

#define AV_PIX_FMT_FLAG_ALPHA        (1 << 7)
static inline int isALPHA(enum AVPixelFormat pix_fmt)
{
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);
//    av_assert0(desc);
    if (pix_fmt == AV_PIX_FMT_PAL8)
        return 1;
    return desc->flags & AV_PIX_FMT_FLAG_ALPHA;
}

/**
 * At least one pixel component is not in the first data plane.
 */
#define AV_PIX_FMT_FLAG_PLANAR       (1 << 4)

/**
 * The pixel format contains RGB-like data (as opposed to YUV/grayscale).
 */
#define AV_PIX_FMT_FLAG_RGB          (1 << 5)

/**
 * Pixel format is big-endian.
 */
#define AV_PIX_FMT_FLAG_BE           (1 << 0)

#define AV_PIX_FMT_BGR32   AV_PIX_FMT_NE(ABGR, RGBA)

#define SWS_BITEXACT          0x80000

#define AV_PIX_FMT_Y400A AV_PIX_FMT_GRAY8A
#define isGray(x)                      \
    ((x) == AV_PIX_FMT_GRAY8       ||  \
     (x) == AV_PIX_FMT_Y400A       ||  \
     (x) == AV_PIX_FMT_GRAY16BE    ||  \
     (x) == AV_PIX_FMT_GRAY16LE)


static inline int isPlanar(enum AVPixelFormat pix_fmt)
{
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);
//    av_assert0(desc);
    return (desc->nb_components >= 2 && (desc->flags & AV_PIX_FMT_FLAG_PLANAR));
}

static inline int isYUV(enum AVPixelFormat pix_fmt)
{
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);
//    av_assert0(desc);
    return !(desc->flags & AV_PIX_FMT_FLAG_RGB) && desc->nb_components >= 2;
}

av_cold int ff_yuv2rgb_c_init_tables(SwsContext *c, const int inv_table[4],
                                     int fullRange, int brightness,
                                     int contrast, int saturation)
{
    const int isRgb = c->dstFormat == AV_PIX_FMT_RGB32     ||
                      c->dstFormat == AV_PIX_FMT_RGB32_1   ||
                      c->dstFormat == AV_PIX_FMT_BGR24     ||
                      c->dstFormat == AV_PIX_FMT_RGB565BE  ||
                      c->dstFormat == AV_PIX_FMT_RGB565LE  ||
                      c->dstFormat == AV_PIX_FMT_RGB555BE  ||
                      c->dstFormat == AV_PIX_FMT_RGB555LE  ||
                      c->dstFormat == AV_PIX_FMT_RGB444BE  ||
                      c->dstFormat == AV_PIX_FMT_RGB444LE  ||
                      c->dstFormat == AV_PIX_FMT_RGB8      ||
                      c->dstFormat == AV_PIX_FMT_RGB4      ||
                      c->dstFormat == AV_PIX_FMT_RGB4_BYTE ||
                      c->dstFormat == AV_PIX_FMT_MONOBLACK;
    const int isNotNe = c->dstFormat == AV_PIX_FMT_NE(RGB565LE, RGB565BE) ||
                        c->dstFormat == AV_PIX_FMT_NE(RGB555LE, RGB555BE) ||
                        c->dstFormat == AV_PIX_FMT_NE(RGB444LE, RGB444BE) ||
                        c->dstFormat == AV_PIX_FMT_NE(BGR565LE, BGR565BE) ||
                        c->dstFormat == AV_PIX_FMT_NE(BGR555LE, BGR555BE) ||
                        c->dstFormat == AV_PIX_FMT_NE(BGR444LE, BGR444BE);
    const int bpp = c->dstFormatBpp;
    uint8_t *y_table;
    uint16_t *y_table16;
    uint32_t *y_table32;
    int i, base, rbase, gbase, bbase, av_uninit(abase), needAlpha;
    const int yoffs = fullRange ? 384 : 326;

    int64_t crv =  inv_table[0];
    int64_t cbu =  inv_table[1];
    int64_t cgu = -inv_table[2];
    int64_t cgv = -inv_table[3];
    int64_t cy  = 1 << 16;
    int64_t oy  = 0;
    int64_t yb  = 0;

    if (!fullRange) {
        cy = (cy * 255) / 219;
        oy = 16 << 16;
    } else {
        crv = (crv * 224) / 255;
        cbu = (cbu * 224) / 255;
        cgu = (cgu * 224) / 255;
        cgv = (cgv * 224) / 255;
    }

    cy   = (cy  * contrast)              >> 16;
    crv  = (crv * contrast * saturation) >> 32;
    cbu  = (cbu * contrast * saturation) >> 32;
    cgu  = (cgu * contrast * saturation) >> 32;
    cgv  = (cgv * contrast * saturation) >> 32;
    oy  -= 256 * brightness;

    c->uOffset = 0x0400040004000400LL;
    c->vOffset = 0x0400040004000400LL;
    c->yCoeff  = roundToInt16(cy  * 8192) * 0x0001000100010001ULL;
    c->vrCoeff = roundToInt16(crv * 8192) * 0x0001000100010001ULL;
    c->ubCoeff = roundToInt16(cbu * 8192) * 0x0001000100010001ULL;
    c->vgCoeff = roundToInt16(cgv * 8192) * 0x0001000100010001ULL;
    c->ugCoeff = roundToInt16(cgu * 8192) * 0x0001000100010001ULL;
    c->yOffset = roundToInt16(oy  *    8) * 0x0001000100010001ULL;

    c->yuv2rgb_y_coeff   = (int16_t)roundToInt16(cy  << 13);
    c->yuv2rgb_y_offset  = (int16_t)roundToInt16(oy  <<  9);
    c->yuv2rgb_v2r_coeff = (int16_t)roundToInt16(crv << 13);
    c->yuv2rgb_v2g_coeff = (int16_t)roundToInt16(cgv << 13);
    c->yuv2rgb_u2g_coeff = (int16_t)roundToInt16(cgu << 13);
    c->yuv2rgb_u2b_coeff = (int16_t)roundToInt16(cbu << 13);

    //scale coefficients by cy
    crv = ((crv << 16) + 0x8000) / FFMAX(cy, 1);
    cbu = ((cbu << 16) + 0x8000) / FFMAX(cy, 1);
    cgu = ((cgu << 16) + 0x8000) / FFMAX(cy, 1);
    cgv = ((cgv << 16) + 0x8000) / FFMAX(cy, 1);

    av_freep(&c->yuvTable);

    switch (bpp) {
    case 1:
        c->yuvTable = av_malloc(1024);
        y_table     = c->yuvTable;
        yb = -(384 << 16) - oy;
        for (i = 0; i < 1024 - 110; i++) {
            y_table[i + 110]  = av_clip_uint8_c((yb + 0x8000) >> 16) >> 7;
            yb               += cy;
        }
        fill_table(c->table_gU, 1, cgu, y_table + yoffs);
        fill_gv_table(c->table_gV, 1, cgv);
        break;
    case 4:
    case 4 | 128:
        rbase       = isRgb ? 3 : 0;
        gbase       = 1;
        bbase       = isRgb ? 0 : 3;
        c->yuvTable = av_malloc(1024 * 3);
        y_table     = c->yuvTable;
        yb = -(384 << 16) - oy;
        for (i = 0; i < 1024 - 110; i++) {
            int yval                = av_clip_uint8_c((yb + 0x8000) >> 16);
            y_table[i + 110]        = (yval >> 7)        << rbase;
            y_table[i +  37 + 1024] = ((yval + 43) / 85) << gbase;
            y_table[i + 110 + 2048] = (yval >> 7)        << bbase;
            yb += cy;
        }
        fill_table(c->table_rV, 1, crv, y_table + yoffs);
        fill_table(c->table_gU, 1, cgu, y_table + yoffs + 1024);
        fill_table(c->table_bU, 1, cbu, y_table + yoffs + 2048);
        fill_gv_table(c->table_gV, 1, cgv);
        break;
    case 8:
        rbase       = isRgb ? 5 : 0;
        gbase       = isRgb ? 2 : 3;
        bbase       = isRgb ? 0 : 6;
        c->yuvTable = av_malloc(1024 * 3);
        y_table     = c->yuvTable;
        yb = -(384 << 16) - oy;
        for (i = 0; i < 1024 - 38; i++) {
            int yval               = av_clip_uint8_c((yb + 0x8000) >> 16);
            y_table[i + 16]        = ((yval + 18) / 36) << rbase;
            y_table[i + 16 + 1024] = ((yval + 18) / 36) << gbase;
            y_table[i + 37 + 2048] = ((yval + 43) / 85) << bbase;
            yb += cy;
        }
        fill_table(c->table_rV, 1, crv, y_table + yoffs);
        fill_table(c->table_gU, 1, cgu, y_table + yoffs + 1024);
        fill_table(c->table_bU, 1, cbu, y_table + yoffs + 2048);
        fill_gv_table(c->table_gV, 1, cgv);
        break;
    case 12:
        rbase       = isRgb ? 8 : 0;
        gbase       = 4;
        bbase       = isRgb ? 0 : 8;
        c->yuvTable = av_malloc(1024 * 3 * 2);
        y_table16   = c->yuvTable;
        yb = -(384 << 16) - oy;
        for (i = 0; i < 1024; i++) {
            uint8_t yval        = av_clip_uint8_c((yb + 0x8000) >> 16);
            y_table16[i]        = (yval >> 4) << rbase;
            y_table16[i + 1024] = (yval >> 4) << gbase;
            y_table16[i + 2048] = (yval >> 4) << bbase;
            yb += cy;
        }
        if (isNotNe)
            for (i = 0; i < 1024 * 3; i++)
                y_table16[i] = av_bswap16(y_table16[i]);
        fill_table(c->table_rV, 2, crv, y_table16 + yoffs);
        fill_table(c->table_gU, 2, cgu, y_table16 + yoffs + 1024);
        fill_table(c->table_bU, 2, cbu, y_table16 + yoffs + 2048);
        fill_gv_table(c->table_gV, 2, cgv);
        break;
    case 15:
    case 16:
        rbase       = isRgb ? bpp - 5 : 0;
        gbase       = 5;
        bbase       = isRgb ? 0 : (bpp - 5);
        c->yuvTable = av_malloc(1024 * 3 * 2);
        y_table16   = c->yuvTable;
        yb = -(384 << 16) - oy;
        for (i = 0; i < 1024; i++) {
            uint8_t yval        = av_clip_uint8_c((yb + 0x8000) >> 16);
            y_table16[i]        = (yval >> 3)          << rbase;
            y_table16[i + 1024] = (yval >> (18 - bpp)) << gbase;
            y_table16[i + 2048] = (yval >> 3)          << bbase;
            yb += cy;
        }
        if (isNotNe)
            for (i = 0; i < 1024 * 3; i++)
                y_table16[i] = av_bswap16(y_table16[i]);
        fill_table(c->table_rV, 2, crv, y_table16 + yoffs);
        fill_table(c->table_gU, 2, cgu, y_table16 + yoffs + 1024);
        fill_table(c->table_bU, 2, cbu, y_table16 + yoffs + 2048);
        fill_gv_table(c->table_gV, 2, cgv);
        break;
    case 24:
    case 48:
        c->yuvTable = av_malloc(1024);
        y_table     = c->yuvTable;
        yb = -(384 << 16) - oy;
        for (i = 0; i < 1024; i++) {
            y_table[i]  = av_clip_uint8_c((yb + 0x8000) >> 16);
            yb         += cy;
        }
        fill_table(c->table_rV, 1, crv, y_table + yoffs);
        fill_table(c->table_gU, 1, cgu, y_table + yoffs);
        fill_table(c->table_bU, 1, cbu, y_table + yoffs);
        fill_gv_table(c->table_gV, 1, cgv);
        break;
    case 32:
    case 64:
        base      = (c->dstFormat == AV_PIX_FMT_RGB32_1 ||
                     c->dstFormat == AV_PIX_FMT_BGR32_1) ? 8 : 0;
        rbase     = base + (isRgb ? 16 : 0);
        gbase     = base + 8;
        bbase     = base + (isRgb ? 0 : 16);
        needAlpha = CONFIG_SWSCALE_ALPHA && isALPHA(c->srcFormat);
        if (!needAlpha)
            abase = (base + 24) & 31;
        c->yuvTable = av_malloc(1024 * 3 * 4);
        y_table32   = c->yuvTable;
        yb = -(384 << 16) - oy;
        for (i = 0; i < 1024; i++) {
            unsigned yval       = av_clip_uint8_c((yb + 0x8000) >> 16);
            y_table32[i]        = (yval << rbase) +
                                  (needAlpha ? 0 : (255u << abase));
            y_table32[i + 1024] =  yval << gbase;
            y_table32[i + 2048] =  yval << bbase;
            yb += cy;
        }
        fill_table(c->table_rV, 4, crv, y_table32 + yoffs);
        fill_table(c->table_gU, 4, cgu, y_table32 + yoffs + 1024);
        fill_table(c->table_bU, 4, cbu, y_table32 + yoffs + 2048);
        fill_gv_table(c->table_gV, 4, cgv);
        break;
    default:
        if(!isPlanar(c->dstFormat) || bpp <= 24)
            LOGD( "%ibpp not supported by yuv2rgb\n", bpp);
        return -1;
    }
    return 0;
}

static int handle_0alpha(enum AVPixelFormat *format)
{
    switch (*format) {
    case AV_PIX_FMT_0BGR    : *format = AV_PIX_FMT_ABGR   ; return 1;
    case AV_PIX_FMT_BGR0    : *format = AV_PIX_FMT_BGRA   ; return 4;
    case AV_PIX_FMT_0RGB    : *format = AV_PIX_FMT_ARGB   ; return 1;
    case AV_PIX_FMT_RGB0    : *format = AV_PIX_FMT_RGBA   ; return 4;
    default:                                          return 0;
    }
}

static int handle_xyz(enum AVPixelFormat *format)
{
    switch (*format) {
    case AV_PIX_FMT_XYZ12BE : *format = AV_PIX_FMT_RGB48BE; return 1;
    case AV_PIX_FMT_XYZ12LE : *format = AV_PIX_FMT_RGB48LE; return 1;
    default:                                                return 0;
    }
}

static void fill_xyztables(struct SwsContext *c)
{
    int i;
    double xyzgamma = XYZ_GAMMA;
    double rgbgamma = 1.0 / RGB_GAMMA;
    double xyzgammainv = 1.0 / XYZ_GAMMA;
    double rgbgammainv = RGB_GAMMA;
    static const int16_t xyz2rgb_matrix[3][4] = {
        {13270, -6295, -2041},
        {-3969,  7682,   170},
        {  228,  -835,  4329} };
    static const int16_t rgb2xyz_matrix[3][4] = {
        {1689, 1464,  739},
        { 871, 2929,  296},
        {  79,  488, 3891} };
    static int16_t xyzgamma_tab[4096], rgbgamma_tab[4096], xyzgammainv_tab[4096], rgbgammainv_tab[4096];

    memcpy(c->xyz2rgb_matrix, xyz2rgb_matrix, sizeof(c->xyz2rgb_matrix));
    memcpy(c->rgb2xyz_matrix, rgb2xyz_matrix, sizeof(c->rgb2xyz_matrix));
    c->xyzgamma = xyzgamma_tab;
    c->rgbgamma = rgbgamma_tab;
    c->xyzgammainv = xyzgammainv_tab;
    c->rgbgammainv = rgbgammainv_tab;

    if (rgbgamma_tab[4095])
        return;

    /* set gamma vectors */
    for (i = 0; i < 4096; i++) {
        xyzgamma_tab[i] = lrint(pow(i / 4095.0, xyzgamma) * 4095.0);
        rgbgamma_tab[i] = lrint(pow(i / 4095.0, rgbgamma) * 4095.0);
        xyzgammainv_tab[i] = lrint(pow(i / 4095.0, xyzgammainv) * 4095.0);
        rgbgammainv_tab[i] = lrint(pow(i / 4095.0, rgbgammainv) * 4095.0);
    }
}

static void handle_formats(SwsContext *c)
{
    c->src0Alpha |= handle_0alpha(&c->srcFormat);
    c->dst0Alpha |= handle_0alpha(&c->dstFormat);
    c->srcXYZ    |= handle_xyz(&c->srcFormat);
    c->dstXYZ    |= handle_xyz(&c->dstFormat);
    if (c->srcXYZ || c->dstXYZ)
        fill_xyztables(c);
}


static void fill_rgb2yuv_table(SwsContext *c, const int table[4], int dstRange)
{
    int64_t W, V, Z, Cy, Cu, Cv;
    int64_t vr =  table[0];
    int64_t ub =  table[1];
    int64_t ug = -table[2];
    int64_t vg = -table[3];
    int64_t ONE = 65536;
    int64_t cy = ONE;
    uint8_t *p = (uint8_t*)c->input_rgb2yuv_table;
    int i;
    static const int8_t map[] = {
    BY_IDX, GY_IDX, -1    , BY_IDX, BY_IDX, GY_IDX, -1    , BY_IDX,
    RY_IDX, -1    , GY_IDX, RY_IDX, RY_IDX, -1    , GY_IDX, RY_IDX,
    RY_IDX, GY_IDX, -1    , RY_IDX, RY_IDX, GY_IDX, -1    , RY_IDX,
    BY_IDX, -1    , GY_IDX, BY_IDX, BY_IDX, -1    , GY_IDX, BY_IDX,
    BU_IDX, GU_IDX, -1    , BU_IDX, BU_IDX, GU_IDX, -1    , BU_IDX,
    RU_IDX, -1    , GU_IDX, RU_IDX, RU_IDX, -1    , GU_IDX, RU_IDX,
    RU_IDX, GU_IDX, -1    , RU_IDX, RU_IDX, GU_IDX, -1    , RU_IDX,
    BU_IDX, -1    , GU_IDX, BU_IDX, BU_IDX, -1    , GU_IDX, BU_IDX,
    BV_IDX, GV_IDX, -1    , BV_IDX, BV_IDX, GV_IDX, -1    , BV_IDX,
    RV_IDX, -1    , GV_IDX, RV_IDX, RV_IDX, -1    , GV_IDX, RV_IDX,
    RV_IDX, GV_IDX, -1    , RV_IDX, RV_IDX, GV_IDX, -1    , RV_IDX,
    BV_IDX, -1    , GV_IDX, BV_IDX, BV_IDX, -1    , GV_IDX, BV_IDX,
    RY_IDX, BY_IDX, RY_IDX, BY_IDX, RY_IDX, BY_IDX, RY_IDX, BY_IDX,
    BY_IDX, RY_IDX, BY_IDX, RY_IDX, BY_IDX, RY_IDX, BY_IDX, RY_IDX,
    GY_IDX, -1    , GY_IDX, -1    , GY_IDX, -1    , GY_IDX, -1    ,
    -1    , GY_IDX, -1    , GY_IDX, -1    , GY_IDX, -1    , GY_IDX,
    RU_IDX, BU_IDX, RU_IDX, BU_IDX, RU_IDX, BU_IDX, RU_IDX, BU_IDX,
    BU_IDX, RU_IDX, BU_IDX, RU_IDX, BU_IDX, RU_IDX, BU_IDX, RU_IDX,
    GU_IDX, -1    , GU_IDX, -1    , GU_IDX, -1    , GU_IDX, -1    ,
    -1    , GU_IDX, -1    , GU_IDX, -1    , GU_IDX, -1    , GU_IDX,
    RV_IDX, BV_IDX, RV_IDX, BV_IDX, RV_IDX, BV_IDX, RV_IDX, BV_IDX,
    BV_IDX, RV_IDX, BV_IDX, RV_IDX, BV_IDX, RV_IDX, BV_IDX, RV_IDX,
    GV_IDX, -1    , GV_IDX, -1    , GV_IDX, -1    , GV_IDX, -1    ,
    -1    , GV_IDX, -1    , GV_IDX, -1    , GV_IDX, -1    , GV_IDX, //23
    -1    , -1    , -1    , -1    , -1    , -1    , -1    , -1    , //24
    -1    , -1    , -1    , -1    , -1    , -1    , -1    , -1    , //25
    -1    , -1    , -1    , -1    , -1    , -1    , -1    , -1    , //26
    -1    , -1    , -1    , -1    , -1    , -1    , -1    , -1    , //27
    -1    , -1    , -1    , -1    , -1    , -1    , -1    , -1    , //28
    -1    , -1    , -1    , -1    , -1    , -1    , -1    , -1    , //29
    -1    , -1    , -1    , -1    , -1    , -1    , -1    , -1    , //30
    -1    , -1    , -1    , -1    , -1    , -1    , -1    , -1    , //31
    BY_IDX, GY_IDX, RY_IDX, -1    , -1    , -1    , -1    , -1    , //32
    BU_IDX, GU_IDX, RU_IDX, -1    , -1    , -1    , -1    , -1    , //33
    BV_IDX, GV_IDX, RV_IDX, -1    , -1    , -1    , -1    , -1    , //34
    };

    dstRange = 0; //FIXME range = 1 is handled elsewhere

    if (!dstRange) {
        cy = cy * 255 / 219;
    } else {
        vr = vr * 224 / 255;
        ub = ub * 224 / 255;
        ug = ug * 224 / 255;
        vg = vg * 224 / 255;
    }
    W = ROUNDED_DIV(ONE*ONE*ug, ub);
    V = ROUNDED_DIV(ONE*ONE*vg, vr);
    Z = ONE*ONE-W-V;

    Cy = ROUNDED_DIV(cy*Z, ONE);
    Cu = ROUNDED_DIV(ub*Z, ONE);
    Cv = ROUNDED_DIV(vr*Z, ONE);

    c->input_rgb2yuv_table[RY_IDX] = -ROUNDED_DIV((1 << RGB2YUV_SHIFT)*V        , Cy);
    c->input_rgb2yuv_table[GY_IDX] =  ROUNDED_DIV((1 << RGB2YUV_SHIFT)*ONE*ONE  , Cy);
    c->input_rgb2yuv_table[BY_IDX] = -ROUNDED_DIV((1 << RGB2YUV_SHIFT)*W        , Cy);

    c->input_rgb2yuv_table[RU_IDX] =  ROUNDED_DIV((1 << RGB2YUV_SHIFT)*V        , Cu);
    c->input_rgb2yuv_table[GU_IDX] = -ROUNDED_DIV((1 << RGB2YUV_SHIFT)*ONE*ONE  , Cu);
    c->input_rgb2yuv_table[BU_IDX] =  ROUNDED_DIV((1 << RGB2YUV_SHIFT)*(Z+W)    , Cu);

    c->input_rgb2yuv_table[RV_IDX] =  ROUNDED_DIV((1 << RGB2YUV_SHIFT)*(V+Z)    , Cv);
    c->input_rgb2yuv_table[GV_IDX] = -ROUNDED_DIV((1 << RGB2YUV_SHIFT)*ONE*ONE  , Cv);
    c->input_rgb2yuv_table[BV_IDX] =  ROUNDED_DIV((1 << RGB2YUV_SHIFT)*W        , Cv);

    if(/*!dstRange && */!memcmp(table, ff_yuv2rgb_coeffs[SWS_CS_DEFAULT], sizeof(ff_yuv2rgb_coeffs[SWS_CS_DEFAULT]))) {
        c->input_rgb2yuv_table[BY_IDX] =  ((int)(0.114 * 219 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
        c->input_rgb2yuv_table[BV_IDX] = (-(int)(0.081 * 224 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
        c->input_rgb2yuv_table[BU_IDX] =  ((int)(0.500 * 224 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
        c->input_rgb2yuv_table[GY_IDX] =  ((int)(0.587 * 219 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
        c->input_rgb2yuv_table[GV_IDX] = (-(int)(0.419 * 224 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
        c->input_rgb2yuv_table[GU_IDX] = (-(int)(0.331 * 224 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
        c->input_rgb2yuv_table[RY_IDX] =  ((int)(0.299 * 219 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
        c->input_rgb2yuv_table[RV_IDX] =  ((int)(0.500 * 224 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
        c->input_rgb2yuv_table[RU_IDX] = (-(int)(0.169 * 224 / 255 * (1 << RGB2YUV_SHIFT) + 0.5));
    }
    for(i=0; i<FF_ARRAY_ELEMS(map); i++)
        AV_WL16(p + 16*4 + 2*i, map[i] >= 0 ? c->input_rgb2yuv_table[map[i]] : 0);
}

int av_get_bits_per_pixel(const AVPixFmtDescriptor *pixdesc)
{
    int c, bits = 0;
    int log2_pixels = pixdesc->log2_chroma_w + pixdesc->log2_chroma_h;

    for (c = 0; c < pixdesc->nb_components; c++) {
        int s = c == 1 || c == 2 ? 0 : log2_pixels;
        bits += (pixdesc->comp[c].depth_minus1 + 1) << s;
    }

    return bits >> log2_pixels;
}

static void xyz12Torgb48(struct SwsContext *c, uint16_t *dst,
                         const uint16_t *src, int stride, int h)
{
    int xp,yp;
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(c->srcFormat);

    for (yp=0; yp<h; yp++) {
        for (xp=0; xp+2<stride; xp+=3) {
            int x, y, z, r, g, b;

            if (desc->flags & AV_PIX_FMT_FLAG_BE) {
                x = AV_RB16(src + xp + 0);
                y = AV_RB16(src + xp + 1);
                z = AV_RB16(src + xp + 2);
            } else {
                x = AV_RL16(src + xp + 0);
                y = AV_RL16(src + xp + 1);
                z = AV_RL16(src + xp + 2);
            }

            x = c->xyzgamma[x>>4];
            y = c->xyzgamma[y>>4];
            z = c->xyzgamma[z>>4];

            // convert from XYZlinear to sRGBlinear
            r = c->xyz2rgb_matrix[0][0] * x +
                c->xyz2rgb_matrix[0][1] * y +
                c->xyz2rgb_matrix[0][2] * z >> 12;
            g = c->xyz2rgb_matrix[1][0] * x +
                c->xyz2rgb_matrix[1][1] * y +
                c->xyz2rgb_matrix[1][2] * z >> 12;
            b = c->xyz2rgb_matrix[2][0] * x +
                c->xyz2rgb_matrix[2][1] * y +
                c->xyz2rgb_matrix[2][2] * z >> 12;

            // limit values to 12-bit depth
            r = av_clip_c(r,0,4095);
            g = av_clip_c(g,0,4095);
            b = av_clip_c(b,0,4095);

            // convert from sRGBlinear to RGB and scale from 12bit to 16bit
            if (desc->flags & AV_PIX_FMT_FLAG_BE) {
                AV_WB16(dst + xp + 0, c->rgbgamma[r] << 4);
                AV_WB16(dst + xp + 1, c->rgbgamma[g] << 4);
                AV_WB16(dst + xp + 2, c->rgbgamma[b] << 4);
            } else {
                AV_WL16(dst + xp + 0, c->rgbgamma[r] << 4);
                AV_WL16(dst + xp + 1, c->rgbgamma[g] << 4);
                AV_WL16(dst + xp + 2, c->rgbgamma[b] << 4);
            }
        }
        src += stride;
        dst += stride;
    }
}


static void reset_ptr(const uint8_t *src[], int format)
{
    if (!isALPHA(format))
        src[3] = NULL;
    if (!isPlanar(format)) {
        src[3] = src[2] = NULL;

//        if (!usePal(format))
//            src[1] = NULL;
    }
}


static void rgb48Toxyz12(struct SwsContext *c, uint16_t *dst,
                         const uint16_t *src, int stride, int h)
{
    int xp,yp;
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(c->srcFormat);

    for (yp=0; yp<h; yp++) {
        for (xp=0; xp+2<stride; xp+=3) {
            int x, y, z, r, g, b;

            if (desc->flags & AV_PIX_FMT_FLAG_BE) {
                r = AV_RB16(src + xp + 0);
                g = AV_RB16(src + xp + 1);
                b = AV_RB16(src + xp + 2);
            } else {
                r = AV_RL16(src + xp + 0);
                g = AV_RL16(src + xp + 1);
                b = AV_RL16(src + xp + 2);
            }

            r = c->rgbgammainv[r>>4];
            g = c->rgbgammainv[g>>4];
            b = c->rgbgammainv[b>>4];

            // convert from sRGBlinear to XYZlinear
            x = c->rgb2xyz_matrix[0][0] * r +
                c->rgb2xyz_matrix[0][1] * g +
                c->rgb2xyz_matrix[0][2] * b >> 12;
            y = c->rgb2xyz_matrix[1][0] * r +
                c->rgb2xyz_matrix[1][1] * g +
                c->rgb2xyz_matrix[1][2] * b >> 12;
            z = c->rgb2xyz_matrix[2][0] * r +
                c->rgb2xyz_matrix[2][1] * g +
                c->rgb2xyz_matrix[2][2] * b >> 12;

            // limit values to 12-bit depth
            x = av_clip_c(x,0,4095);
            y = av_clip_c(y,0,4095);
            z = av_clip_c(z,0,4095);

            // convert from XYZlinear to X'Y'Z' and scale from 12bit to 16bit
            if (desc->flags & AV_PIX_FMT_FLAG_BE) {
                AV_WB16(dst + xp + 0, c->xyzgammainv[x] << 4);
                AV_WB16(dst + xp + 1, c->xyzgammainv[y] << 4);
                AV_WB16(dst + xp + 2, c->xyzgammainv[z] << 4);
            } else {
                AV_WL16(dst + xp + 0, c->xyzgammainv[x] << 4);
                AV_WL16(dst + xp + 1, c->xyzgammainv[y] << 4);
                AV_WL16(dst + xp + 2, c->xyzgammainv[z] << 4);
            }
        }
        src += stride;
        dst += stride;
    }
}



#    define attribute_align_arg __attribute__((force_align_arg_pointer))
/**
 * swscale wrapper, so we don't need to export the SwsContext.
 * Assumes planar YUV to be in YUV order instead of YVU.
 */
int attribute_align_arg sws_scale(struct SwsContext *c,
                                  const uint8_t * const srcSlice[],
                                  const int srcStride[], int srcSliceY,
                                  int srcSliceH, uint8_t *const dst[],
                                  const int dstStride[])
{
//	LOGD("sws_scale Called\n");
    int i, ret;
    const uint8_t *src2[4];
    uint8_t *dst2[4];
    uint8_t *rgb0_tmp = NULL;

    if (!srcSlice || !dstStride || !dst || !srcSlice) {
    	LOGD( "One of the input parameters to sws_scale() is NULL, please check the calling code\n");
        return 0;
    }
    memcpy(src2, srcSlice, sizeof(src2));
    memcpy(dst2, dst, sizeof(dst2));

    // do not mess up sliceDir if we have a "trailing" 0-size slice
    if (srcSliceH == 0)
        return 0;
//チェックを端折った
//    if (!check_image_pointers(srcSlice, c->srcFormat, srcStride)) {
//        LOGD( "bad src image pointers\n");
//        return 0;
//    }
//    if (!check_image_pointers((const uint8_t* const*)dst, c->dstFormat, dstStride)) {
//    	LOGD( "bad dst image pointers\n");
//        return 0;
//    }

    if (c->sliceDir == 0 && srcSliceY != 0 && srcSliceY + srcSliceH != c->srcH) {
    	LOGD( "Slices start in the middle!\n");
        return 0;
    }
    if (c->sliceDir == 0) {
        if (srcSliceY == 0) c->sliceDir = 1; else c->sliceDir = -1;
    }


    if (c->src0Alpha && !c->dst0Alpha && isALPHA(c->dstFormat)) {
        uint8_t *base;
        int x,y;
        rgb0_tmp = av_malloc(FFABS(srcStride[0]) * srcSliceH + 32);
        if (!rgb0_tmp)
            return -1;

        base = srcStride[0] < 0 ? rgb0_tmp - srcStride[0] * (srcSliceH-1) : rgb0_tmp;
        for (y=0; y<srcSliceH; y++){
            memcpy(base + srcStride[0]*y, src2[0] + srcStride[0]*y, 4*c->srcW);
            for (x=c->src0Alpha-1; x<4*c->srcW; x+=4) {
                base[ srcStride[0]*y + x] = 0xFF;
            }
        }
        src2[0] = base;
    }

    if (c->srcXYZ && !(c->dstXYZ && c->srcW==c->dstW && c->srcH==c->dstH)) {
        uint8_t *base;
        rgb0_tmp = av_malloc(FFABS(srcStride[0]) * srcSliceH + 32);
        if (!rgb0_tmp)
            return -1;

        base = srcStride[0] < 0 ? rgb0_tmp - srcStride[0] * (srcSliceH-1) : rgb0_tmp;

        xyz12Torgb48(c, (uint16_t*)base, (const uint16_t*)src2[0], srcStride[0]/2, srcSliceH);
        src2[0] = base;
    }

    if (!srcSliceY && (c->flags & SWS_BITEXACT) && c->dither == SWS_DITHER_ED && c->dither_error[0])
        for (i = 0; i < 4; i++)
            memset(c->dither_error[i], 0, sizeof(c->dither_error[0][0]) * (c->dstW+2));


    // copy strides, so they can safely be modified
    if (c->sliceDir == 1) {
//    	int index;
//    	for(index = 0; index < 4; index++){
//    		LOGD("srcStride[%d]:%d\n",index,srcStride[index]);
//    	}
//    	for(index = 0; index < 4; index++){
//    	    		LOGD("dstStride[%d]:%d\n",index,dstStride[index]);
//    	    	}
    	// slices go from top to bottom
        int srcStride2[4] = { srcStride[0], srcStride[1], srcStride[2],
                              srcStride[3] };
        int dstStride2[4] = { dstStride[0], dstStride[1], dstStride[2],
                              dstStride[3] };

        reset_ptr(src2, c->srcFormat);
        reset_ptr((void*)dst2, c->dstFormat);

        /* reset slice direction at end of frame */
        if (srcSliceY + srcSliceH == c->srcH)
            c->sliceDir = 0;
        LOGD("sws_scale A Before\n");
        ret = c->swscale(c, src2, srcStride2, srcSliceY, srcSliceH, dst2, dstStride2);
    } else {
    	LOGD( "sws_scale F\n");
        // slices go from bottom to top => we flip the image internally
        int srcStride2[4] = { -srcStride[0], -srcStride[1], -srcStride[2],
                              -srcStride[3] };
        int dstStride2[4] = { -dstStride[0], -dstStride[1], -dstStride[2],
                              -dstStride[3] };

        src2[0] += (srcSliceH - 1) * srcStride[0];
//        if (!usePal(c->srcFormat))
//            src2[1] += ((srcSliceH >> c->chrSrcVSubSample) - 1) * srcStride[1];
        src2[2] += ((srcSliceH >> c->chrSrcVSubSample) - 1) * srcStride[2];
        src2[3] += (srcSliceH - 1) * srcStride[3];
        dst2[0] += ( c->dstH                         - 1) * dstStride[0];
        dst2[1] += ((c->dstH >> c->chrDstVSubSample) - 1) * dstStride[1];
        dst2[2] += ((c->dstH >> c->chrDstVSubSample) - 1) * dstStride[2];
        dst2[3] += ( c->dstH                         - 1) * dstStride[3];

        reset_ptr(src2, c->srcFormat);
        reset_ptr((void*)dst2, c->dstFormat);

        /* reset slice direction at end of frame */
        if (!srcSliceY)
            c->sliceDir = 0;

        ret = c->swscale(c, src2, srcStride2, c->srcH-srcSliceY-srcSliceH,
                          srcSliceH, dst2, dstStride2);
    }


    if (c->dstXYZ && !(c->srcXYZ && c->srcW==c->dstW && c->srcH==c->dstH)) {
        /* replace on the same data */
        rgb48Toxyz12(c, (uint16_t*)dst2[0], (const uint16_t*)dst2[0], dstStride[0]/2, ret);
    }

    av_free(rgb0_tmp);
    return ret;
}

int sws_setColorspaceDetails(struct SwsContext *c, const int inv_table[4],
                             int srcRange, const int table[4], int dstRange,
                             int brightness, int contrast, int saturation)
{
//	LOGD("sws_setColorspaceDetails Called\n");
    const AVPixFmtDescriptor *desc_dst;
    const AVPixFmtDescriptor *desc_src;
    memmove(c->srcColorspaceTable, inv_table, sizeof(int) * 4);
    memmove(c->dstColorspaceTable, table, sizeof(int) * 4);

    handle_formats(c);
    desc_dst = av_pix_fmt_desc_get(c->dstFormat);
    desc_src = av_pix_fmt_desc_get(c->srcFormat);

//    LOGD("sws_setColorspaceDetails dst name:%s src name:%s\n",desc_dst->name,desc_src->name);
    if(!isYUV(c->dstFormat) && !isGray(c->dstFormat))
        dstRange = 0;
    if(!isYUV(c->srcFormat) && !isGray(c->srcFormat))
        srcRange = 0;

    c->brightness = brightness;
    c->contrast   = contrast;
    c->saturation = saturation;
    c->srcRange   = srcRange;
    c->dstRange   = dstRange;

    if ((isYUV(c->dstFormat) || isGray(c->dstFormat)) && (isYUV(c->srcFormat) || isGray(c->srcFormat)))
        return -1;

    c->dstFormatBpp = av_get_bits_per_pixel(desc_dst);
    c->srcFormatBpp = av_get_bits_per_pixel(desc_src);

    if (!isYUV(c->dstFormat) && !isGray(c->dstFormat)) {
        ff_yuv2rgb_c_init_tables(c, inv_table, srcRange, brightness,
                                 contrast, saturation);
        // FIXME factorize

//        if (ARCH_PPC)
//            ff_yuv2rgb_init_tables_ppc(c, inv_table, brightness,
//                                       contrast, saturation);
    }

    fill_rgb2yuv_table(c, table, dstRange);

    return 0;
}


static int scale_slice(ScaleContext *scale, AVFrame *out_buf, AVFrame *cur_pic, struct SwsContext *sws, int y, int h, int mul, int field)
{
//	LOGD("scale_slice Called\n");
    const uint8_t *in[4];
    uint8_t *out[4];
    int in_stride[4],out_stride[4];
    int i;
//    for(i = 0; i < 10; i++){
//    	LOGD("%02X ",cur_pic->data[0][i]);
//    }
//    LOGD("\n");
//    for(i = 0; i < 10; i++){
//        	LOGD("%02X ",out_buf->data[0][i]);
//	}
//	LOGD("\n");
    for(i=0; i<4; i++){
        int vsub= ((i+1)&2) ? scale->vsub : 0;
         in_stride[i] = cur_pic->linesize[i] * mul;
        out_stride[i] = out_buf->linesize[i] * mul;
         in[i] = cur_pic->data[i] + ((y>>vsub)+field) * cur_pic->linesize[i];
        out[i] = out_buf->data[i] +            field  * out_buf->linesize[i];
    }
    if(scale->input_is_pal)
         in[1] = cur_pic->data[1];
    if(scale->output_is_pal)
        out[1] = out_buf->data[1];
//    LOGD("scale_slice params in_stride[0]:%d y:%d mul:%d h:%d out_stride[0]:%d\n",in_stride[0],y,mul,h,out_stride[0]);
    return sws_scale(sws, in, in_stride, y/mul, h,
                         out,out_stride);
}

enum AVColorSpace av_frame_get_colorspace(const AVFrame *frame){
	return 2;//固定の値を返す 毎回2
}



static void copyPlane(const uint8_t *src, int srcStride,
                      int srcSliceY, int srcSliceH, int width,
                      uint8_t *dst, int dstStride)
{//ストリーム送信が終わっているのにここが呼ばれると落ちる
		LOGD("copyPlane Called");
    dst += dstStride * srcSliceY;
    if (dstStride == srcStride && srcStride > 0) {
        memcpy(dst, src, srcSliceH * dstStride);
    } else {
        int i;
        for (i = 0; i < srcSliceH; i++) {
            memcpy(dst, src, width);
            src += srcStride;
            dst += dstStride;
        }
    }
}


#define DITHER_COPY(dst, dstStride, src, srcStride, bswap, dbswap)\
    uint16_t scale= dither_scale[dst_depth-1][src_depth-1];\
    int shift= src_depth-dst_depth + dither_scale[src_depth-2][dst_depth-1];\
    for (i = 0; i < height; i++) {\
        const uint8_t *dither= dithers[src_depth-9][i&7];\
        for (j = 0; j < length-7; j+=8){\
            dst[j+0] = dbswap((bswap(src[j+0]) + dither[0])*scale>>shift);\
            dst[j+1] = dbswap((bswap(src[j+1]) + dither[1])*scale>>shift);\
            dst[j+2] = dbswap((bswap(src[j+2]) + dither[2])*scale>>shift);\
            dst[j+3] = dbswap((bswap(src[j+3]) + dither[3])*scale>>shift);\
            dst[j+4] = dbswap((bswap(src[j+4]) + dither[4])*scale>>shift);\
            dst[j+5] = dbswap((bswap(src[j+5]) + dither[5])*scale>>shift);\
            dst[j+6] = dbswap((bswap(src[j+6]) + dither[6])*scale>>shift);\
            dst[j+7] = dbswap((bswap(src[j+7]) + dither[7])*scale>>shift);\
        }\
        for (; j < length; j++)\
            dst[j] = dbswap((bswap(src[j]) + dither[j&7])*scale>>shift);\
        dst += dstStride;\
        src += srcStride;\
    }


#define isNBPS(x) is9_OR_10BPS(x)


#define FF_CEIL_RSHIFT(a,b) (!av_builtin_constant_p(b) ? -((-(a)) >> (b)) \
                                                       : ((a) + (1<<(b)) - 1) >> (b))

#   define AV_RN32A(p) AV_RNA(32, p)

#define AV_WNA(s, p, v) (((av_alias##s*)(p))->u##s = (v))
#   define AV_WN32A(p, v) AV_WNA(32, p, v)

static void deinterleaveBytes_c(const uint8_t *src, uint8_t *dst1, uint8_t *dst2,
                                int width, int height, int srcStride,
                                int dst1Stride, int dst2Stride)
{
	LOGD("deinterleaveBytes_c Called\n");
    int h;

    for (h = 0; h < height; h++) {
        int w;
        for (w = 0; w < width; w++) {
            dst1[w] = src[2 * w + 0];
            dst2[w] = src[2 * w + 1];
        }
        src += srcStride;
        dst1 += dst1Stride;
        dst2 += dst2Stride;
    }
}

static int nv12ToPlanarWrapper(SwsContext *c, const uint8_t *src[],
                               int srcStride[], int srcSliceY,
                               int srcSliceH, uint8_t *dstParam[],
                               int dstStride[])
{
//	LOGD("nv12ToPlanarWrapper Called");
	LOGD("nv12ToPlanarWrapper params src:%d srcStride:%d srcSliceY:%d srcSliceH:%d dst:%d dstStride:%d\n",src,srcStride,srcSliceY,srcSliceH,dstParam,dstStride);

    uint8_t *dst1 = dstParam[1] + dstStride[1] * srcSliceY / 2;
    uint8_t *dst2 = dstParam[2] + dstStride[2] * srcSliceY / 2;

    copyPlane(src[0], srcStride[0], srcSliceY, srcSliceH, c->srcW,
              dstParam[0], dstStride[0]);

//    if (c->srcFormat == AV_PIX_FMT_NV12)
//        deinterleaveBytes_c(src[1], dst1, dst2,c->srcW / 2, srcSliceH / 2,
//                          srcStride[1], dstStride[1], dstStride[2]);
//    else
        deinterleaveBytes_c(src[1], dst2, dst1, c->srcW / 2, srcSliceH / 2,
                          srcStride[1], dstStride[2], dstStride[1]);

    return srcSliceH;
}

//encode2
int ff_MPV_encode_picture(AVCodecContext *avctx, AVPacket *pkt,
                          AVFrame *pic_arg, int *got_packet)
{
//    LOGD( "MPV_encode_picture Called frame_number:%d\n",avctx->frame_number);
    MpegEncContext *s = avctx->priv_data;
    int i, stuffing_count, ret;
    int context_count = s->slice_context_count;
    s->picture_in_gop_number++;
    LOGD("TOTAL_BITS_A:%08X",s->total_bits);
//    LOGD("test_q_scale:%d\n",s->qscale);

    //ここで毎回sampleaspectはden:1、num:0 context_countは1 pict_typeは毎回0でくる
//------------------------------
    AVFrame* out = (AVFrame*)avcodec_alloc_frame();
    av_frame_copy_props(out, pic_arg);//引数のフレームの属性をコピーする

    int size = avctx->width*avctx->height;
    out->data[0] = av_malloc(size * 3 / 2); /* size for YUV 420 */
    out->data[1] = out->data[0] + size;//U
    out->data[2] = out->data[1] + size / 4;//V
    //get_linesizeはここでは失敗しないと思われる
    get_linesizes(out->linesize,avctx->width,AV_PIX_FMT_YUV420P);

    int in_full, out_full, brightness, contrast, saturation;
    const int *inv_table, *table;
    ScaleContext *scale = av_malloc(sizeof(ScaleContext));
    memset(scale,0,sizeof(ScaleContext));
    scale->sws = av_malloc(sizeof(SwsContext));
    memset(scale->sws,0,sizeof(SwsContext));
    scale->sws->srcW = avctx->width;
    scale->sws->srcH = avctx->height;
    scale->sws->dstFormat = AV_PIX_FMT_YUV420P;
    scale->sws->srcFormat = AV_PIX_FMT_NV21;
    scale->in_color_matrix = "auto";
    scale->vsub = 1;

    LOGD("TOTAL_BITS_:%d",s->total_bits);
    //初期化→定数化必要
    sws_setColorspaceDetails(scale->sws, ff_yuv2rgb_coeffs[SWS_CS_DEFAULT], scale->sws->srcRange,
                             ff_yuv2rgb_coeffs[SWS_CS_DEFAULT],
                             scale->sws->dstRange, 0, 1 << 16, 1 << 16);
    scale->sws->swscale = nv12ToPlanarWrapper;

//    LOGD("sws_init_context setColorspaceD After brightness:%d contrast:%d saturation:%d srcRange:%d dstRange:%d out_full:%d\n",scale->sws->brightness,scale->sws->contrast,scale->sws->saturation,scale->sws->srcRange,scale->sws->dstRange,out_full);
    //初期化された値を使ってセット
    sws_getColorspaceDetails(scale->sws, (int **)&inv_table, &in_full,
                             (int **)&table, &out_full,
                             &brightness, &contrast, &saturation);

    if (scale->in_color_matrix)
        inv_table = parse_yuv_type(scale->in_color_matrix, av_frame_get_colorspace(pic_arg));
    if (scale->out_color_matrix)
        table     = parse_yuv_type(scale->out_color_matrix, AVCOL_SPC_UNSPECIFIED);

    if (scale-> in_range != AVCOL_RANGE_UNSPECIFIED)
        in_full  = (scale-> in_range == AVCOL_RANGE_JPEG);
    if (scale->out_range != AVCOL_RANGE_UNSPECIFIED)
        out_full = (scale->out_range == AVCOL_RANGE_JPEG);

    sws_setColorspaceDetails(scale->sws, inv_table, in_full,
                             table, out_full,
                             brightness, contrast, saturation);
//    if (scale->isws[0])
//        sws_setColorspaceDetails(scale->isws[0], inv_table, in_full,
//                                 table, out_full,
//                                 brightness, contrast, saturation);
//    if (scale->isws[1])
//        sws_setColorspaceDetails(scale->isws[1], inv_table, in_full,
//                                 table, out_full,
//                                 brightness, contrast, saturation);
//    LOGD("sample_aspect_ratio pic num:%d den:%d out num:%d den:%d\n",pic_arg->sample_aspect_ratio.num,pic_arg->sample_aspect_ratio.den,out->sample_aspect_ratio.num,out->sample_aspect_ratio.den);
    av_reduce(&out->sample_aspect_ratio.num, &out->sample_aspect_ratio.den,
                  (int64_t)pic_arg->sample_aspect_ratio.num * avctx->height * avctx->width,
                  (int64_t)pic_arg->sample_aspect_ratio.den * avctx->width * avctx->height,
                  INT_MAX);

    //scale_sliceでsws_scaleつまりnv12ToPlanarWrapperが呼ばれる
//        if(scale->interlaced>0 || (scale->interlaced<0 && out->interlaced_frame)){
//            scale_slice(scale, out, pic_arg, scale->isws[0], 0, (avctx->height+1)/2, 2, 0);
//            scale_slice(scale, out, pic_arg, scale->isws[1], 0,  avctx->height   /2, 2, 1);
//        }else{//こっちが呼ばれる
            scale_slice(scale, out, pic_arg, scale->sws, 0, avctx->height, 1, 0);
//        }
//        av_frame_free(&out);
////       free(&scale);
    //-----------------------------
//    LOGD("ff_MPV_encode_picture sampleaspect den:%d num:%d",pic_arg->sample_aspect_ratio.den,pic_arg->sample_aspect_ratio.num);

            LOGD("TOTAL_BITS_X:%d",s->total_bits);
    if (load_input_picture(s, out) < 0)
        return -1;
    if (select_input_picture(s) < 0) {
        return -1;
    }
    LOGD("TOTAL_BITS_:%d",s->total_bits);

    LOGD("TOTAL_BITS_:%d",s->total_bits);
    /* データのアドレスがある場合、ここを通る */
    if (s->new_picture.f.data[0]) {
        if ((ret = ff_alloc_packet2(avctx, pkt, s->mb_width*s->mb_height*(MAX_MB_BYTES+100)+10000)) < 0){
            return ret;
        }
        if (s->mb_info) {
            s->mb_info_ptr = av_packet_new_side_data(pkt,
                                 AV_PKT_DATA_H263_MB_INFO,
                                 s->mb_width*s->mb_height*12);
            s->prev_mb_info = s->last_mb_info = s->mb_info_size = 0;
        }
        //ここでs->pbとavctx->internal->byte_bufferとpkt->dataが設定されてる
        LOGD("ff_MPV_encode_picture context_count:%d",context_count);
        for (i = 0; i < context_count; i++) {
            int start_y = s->thread_context[i]->start_mb_y;
            int   end_y = s->thread_context[i]->  end_mb_y;
            int h       = s->mb_height;
//            LOGD("start_mb_y:%d end_mb_y:%d size:%d\n",start_y,end_y,(end_y-start_y));
            uint8_t *start = pkt->data + (size_t)(((int64_t) pkt->size) * start_y / h);
            uint8_t *end   = pkt->data + (size_t)(((int64_t) pkt->size) *   end_y / h);

            init_put_bits(&s->thread_context[i]->pb, start, end - start);

            //ここでs->pb->bufとavctx->internal->byte_bufferが同じアドレスになる
        }

//       if(pkt){//初回データはここではない
//			LOGD("MPV OUTPUT_DEBUG B size:%d\n",pkt->size);
//		for(i = 0; i < 100 && i< pkt->size; i++){
//			LOGD( "%02X ",pkt->data[i]);
//		}
//		LOGD( "\n");
//	}

        s->pict_type = s->new_picture.f.pict_type;
        //emms_c();
        if (ff_MPV_frame_start(s, avctx) < 0)
            return -1;



vbv_retry:
//LOGD("encode_picture BEFORE size:%d put_bits_count:%d\n",s->frame_bits,put_bits_count(&s->pb));

//LOGD("TOTAL_BITS_:%d",s->total_bits);
        if (encode_picture(s, s->picture_number) < 0)
            return -1;
        //ここで既にput_bits_count(&s->pb)すると出力データサイズが取得できるのでデータがある
		LOGD("encode_picture AFTER size:%d put_bits_count:%d\n",s->frame_bits,put_bits_count(&s->pb));
        avctx->header_bits = s->header_bits;
        avctx->mv_bits     = s->mv_bits;
        avctx->misc_bits   = s->misc_bits;
        avctx->i_tex_bits  = s->i_tex_bits;
        avctx->p_tex_bits  = s->p_tex_bits;
        avctx->i_count     = s->i_count;
        // FIXME f/b_count in avctx
        avctx->p_count     = s->mb_num - s->i_count - s->skip_count;
        avctx->skip_count  = s->skip_count;

//        LOGD("ff_MPV_encode_picture header_bits:%d\n",avctx->header_bits);
//        LOGD("ff_MPV_encode_picture mv_bits:%d\n",avctx->mv_bits);
//        LOGD("ff_MPV_encode_picture misc_bits:%d\n",avctx->misc_bits);
//        LOGD("ff_MPV_encode_picture i_tex_bits:%d\n",avctx->i_tex_bits);
//        LOGD("ff_MPV_encode_picture p_tex_bits:%d\n",avctx->p_tex_bits);
//        LOGD("ff_MPV_encode_picture i_count:%d\n",avctx->i_count);
//        LOGD("ff_MPV_encode_picture p_count:%d\n",avctx->p_count);
//        LOGD("ff_MPV_encode_picture skip_count:%d\n",avctx->skip_count);
//		LOGD("MPV OUTPUT_DEBUG CA size:%d\n",s->frame_bits);
        ff_MPV_frame_end(s);

//        if(pkt){//初回データはここである
//        			LOGD("MPV OUTPUT_DEBUG C size:%d\n",pkt->size);
//        		for(i = 0; i < 100 && i< pkt->size; i++){
//        			LOGD( "%02X ",pkt->data[i]);
//        		}
//        		LOGD( "\n");
//        	}

        if (CONFIG_MJPEG_ENCODER && s->out_format == FMT_MJPEG)
            ff_mjpeg_encode_picture_trailer(s);

        if (avctx->rc_buffer_size) {//初回は通ってない
            RateControlContext *rcc = &s->rc_context;
            int max_size = rcc->buffer_index * avctx->rc_max_available_vbv_use;

            if (put_bits_count(&s->pb) > max_size &&
                s->lambda < s->avctx->lmax) {
                s->next_lambda = FFMAX(s->lambda + 1, s->lambda *
                                       (s->qscale + 1) / s->qscale);
                if (s->adaptive_quant) {
                    int i;
                    for (i = 0; i < s->mb_height * s->mb_stride; i++)
                        s->lambda_table[i] =
                            FFMAX(s->lambda_table[i] + 1,
                                  s->lambda_table[i] * (s->qscale + 1) /
                                  s->qscale);
                }
                s->mb_skipped = 0;        // done in MPV_frame_start()
                // done in encode_picture() so we must undo it
                if (s->pict_type == AV_PICTURE_TYPE_P) {
                    if (s->flipflop_rounding          ||
                        s->codec_id == AV_CODEC_ID_H263P ||
                        s->codec_id == AV_CODEC_ID_MPEG4)
                        s->no_rounding ^= 1;
                }
                if (s->pict_type != AV_PICTURE_TYPE_B) {
                    s->time_base       = s->last_time_base;
                    s->last_non_b_time = s->time - s->pp_time;
                }
                for (i = 0; i < context_count; i++) {
                    PutBitContext *pb = &s->thread_context[i]->pb;
                    init_put_bits(pb, pb->buf, pb->buf_end - pb->buf);
                }
                goto vbv_retry;
            }

//            assert(s->avctx->rc_max_rate);
        }

//        LOGD("s->flags:%d PASS1:%d\n",s->flags,(s->flags & CODEC_FLAG_PASS1));
        if (s->flags & CODEC_FLAG_PASS1)//初回は通ってない
            ff_write_pass1_stats(s);

        for (i = 0; i < 4; i++) {
            s->current_picture_ptr->f.error[i] = s->current_picture.f.error[i];
            avctx->error[i] += s->current_picture_ptr->f.error[i];
        }

//        if (s->flags & CODEC_FLAG_PASS1)//初回は通ってない
//            assert(avctx->header_bits + avctx->mv_bits + avctx->misc_bits +
//                   avctx->i_tex_bits + avctx->p_tex_bits ==
//                       put_bits_count(&s->pb));

        //s->frame_bits/8が出力するサイズになる、ここまででs->pbに出力する準備が出来ていて、flush_bitsで出力サイズは決定している
        flush_put_bits(&s->pb);
        s->frame_bits  = put_bits_count(&s->pb);

//        LOGD("TOTAL_BITS_:%d",s->total_bits);
        stuffing_count = ff_vbv_update(s, s->frame_bits);
        s->stuffing_bits = 8*stuffing_count;
        if (stuffing_count) {
            if (s->pb.buf_end - s->pb.buf - (put_bits_count(&s->pb) >> 3) <
                    stuffing_count + 50) {
                printf( "stuffing too large\n");
                return -1;
            }

            switch (s->codec_id) {
            case AV_CODEC_ID_MPEG1VIDEO:
            case AV_CODEC_ID_MPEG2VIDEO:
                while (stuffing_count--) {
                    put_bits(&s->pb, 8, 0);
                }
            break;
            case AV_CODEC_ID_MPEG4:
                put_bits(&s->pb, 16, 0);
                put_bits(&s->pb, 16, 0x1C3);
                stuffing_count -= 4;
                while (stuffing_count--) {
                    put_bits(&s->pb, 8, 0xFF);
                }
            break;
            default:
            	LOGD( "vbv buffer overflow\n");
            }
            flush_put_bits(&s->pb);
            s->frame_bits  = put_bits_count(&s->pb);
        }

//        if(pkt){
//        		LOGD("OUTPUT_DEBUG E size:%d\n",pkt->size);
//        		for(i = 0; i < 100 && i< pkt->size; i++){
//        			LOGD( "%02X ",pkt->data[i]);
//        		}
//        		LOGD( "\n");
//        }
        /* update mpeg1/2 vbv_delay for CBR */
        if (s->avctx->rc_max_rate                          &&
            s->avctx->rc_min_rate == s->avctx->rc_max_rate &&
            s->out_format == FMT_MPEG1                     &&
            90000LL * (avctx->rc_buffer_size - 1) <=
                s->avctx->rc_max_rate * 0xFFFFLL) {
        	LOGD("delay_logic\n");
            int vbv_delay, min_delay;
            double inbits  = s->avctx->rc_max_rate *
                             av_q2d(s->avctx->time_base);
            int    minbits = s->frame_bits - 8 *
                             (s->vbv_delay_ptr - s->pb.buf - 1);
            double bits    = s->rc_context.buffer_index + minbits - inbits;

            if (bits < 0)
            	LOGD( "Internal error, negative bits\n");

//            assert(s->repeat_first_field == 0);

            vbv_delay = bits * 90000 / s->avctx->rc_max_rate;
            min_delay = (minbits * 90000LL + s->avctx->rc_max_rate - 1) /
                        s->avctx->rc_max_rate;

            vbv_delay = FFMAX(vbv_delay, min_delay);

//            av_assert0(vbv_delay < 0xFFFF);

            s->vbv_delay_ptr[0] &= 0xF8;
            s->vbv_delay_ptr[0] |= vbv_delay >> 13;
            s->vbv_delay_ptr[1]  = vbv_delay >> 5;
            s->vbv_delay_ptr[2] &= 0x07;
            s->vbv_delay_ptr[2] |= vbv_delay << 3;
            avctx->vbv_delay     = vbv_delay * 300;
        }
        LOGD("->total_bits:%d s->frame_bits:%d\n",s->total_bits,s->frame_bits);
        s->total_bits     += s->frame_bits;
        avctx->frame_bits  = s->frame_bits;

//        if(pkt){
//        		LOGD("OUTPUT_DEBUG F size:%d\n",pkt->size);
//        		for(i = 0; i < 100 && i< pkt->size; i++){
//        			LOGD( "%02X ",pkt->data[i]);
//        		}
//        		LOGD( "\n");
//        }
        pkt->pts = s->current_picture.f.pts;
        if (!s->low_delay && s->pict_type != AV_PICTURE_TYPE_B) {
            if (!s->current_picture.f.coded_picture_number)
                pkt->dts = pkt->pts - s->dts_delta;
            else
                pkt->dts = s->reordered_pts;
            s->reordered_pts = pkt->pts;
        } else
            pkt->dts = pkt->pts;
        if (s->current_picture.f.key_frame)
            pkt->flags |= AV_PKT_FLAG_KEY;
        if (s->mb_info)
            av_packet_shrink_side_data(pkt, AV_PKT_DATA_H263_MB_INFO, s->mb_info_size);
    } else {
        s->frame_bits = 0;
    }
//    assert((s->frame_bits & 7) == 0);
    pkt->size = s->frame_bits / 8;
    *got_packet = !!pkt->size;


    //ここでパケットのデータ部分は出来上がっているがPTSはまだない
//    if(pkt){
//			LOGD("OUTPUT_DEBUG size:%d\n",pkt->size);
//		for(i = 0; i < 100; i++){
//			LOGD( "%02X ",pkt->data[i]);
//		}
//		LOGD( "\n");
//	}
//    LOGD( "MPV_encode_picture standard return:%d\n",pkt->size);
    return 0;
}

int av_samples_set_silence(uint8_t **audio_data, int offset, int nb_samples,
                           int nb_channels, enum AVSampleFormat sample_fmt)
{
    int planar      = av_sample_fmt_is_planar(sample_fmt);
    int planes      = planar ? nb_channels : 1;
    int block_align = av_get_bytes_per_sample(sample_fmt) * (planar ? 1 : nb_channels);
    int data_size   = nb_samples * block_align;
    int fill_char   = (sample_fmt == AV_SAMPLE_FMT_U8 ||
                     sample_fmt == AV_SAMPLE_FMT_U8P) ? 0x80 : 0x00;
    int i;

    offset *= block_align;

    for (i = 0; i < planes; i++)
        memset(audio_data[i] + offset, fill_char, data_size);

    return 0;
}
int av_samples_copy(uint8_t **dst, uint8_t * const *src, int dst_offset,
                    int src_offset, int nb_samples, int nb_channels,
                    enum AVSampleFormat sample_fmt)
{
    int planar      = av_sample_fmt_is_planar(sample_fmt);
    int planes      = planar ? nb_channels : 1;
    int block_align = av_get_bytes_per_sample(sample_fmt) * (planar ? 1 : nb_channels);
    int data_size   = nb_samples * block_align;
    int i;

    dst_offset *= block_align;
    src_offset *= block_align;

    if((dst[0] < src[0] ? src[0] - dst[0] : dst[0] - src[0]) >= data_size) {
        for (i = 0; i < planes; i++)
            memcpy(dst[i] + dst_offset, src[i] + src_offset, data_size);
    } else {
        for (i = 0; i < planes; i++)
            memmove(dst[i] + dst_offset, src[i] + src_offset, data_size);
    }

    return 0;
}
/**
 * Pad last frame with silence.
 */
static int pad_last_frame(AVCodecContext *s, AVFrame **dst, const AVFrame *src)
{

    AVFrame *frame = NULL;
    uint8_t *buf   = NULL;
    int ret;

    if (!(frame = avcodec_alloc_frame()))
        return -1;
    *frame = *src;

    if ((ret = av_samples_get_buffer_size(&frame->linesize[0], s->channels,
                                          s->frame_size, s->sample_fmt, 0)) < 0)
        goto fail;

    if (!(buf = av_malloc(ret))) {
        ret = -1;
        goto fail;
    }

    frame->nb_samples = s->frame_size;
    if ((ret = avcodec_fill_audio_frame(frame, s->channels, s->sample_fmt,
                                        buf, ret, 0)) < 0)
        goto fail;
    if ((ret = av_samples_copy(frame->extended_data, src->extended_data, 0, 0,
                               src->nb_samples, s->channels, s->sample_fmt)) < 0)
        goto fail;
    if ((ret = av_samples_set_silence(frame->extended_data, src->nb_samples,
                                      frame->nb_samples - src->nb_samples,
                                      s->channels, s->sample_fmt)) < 0)
        goto fail;

    *dst = frame;

    return 0;

fail:
    if (frame->extended_data != frame->data)
        av_freep(&frame->extended_data);
    av_freep(&buf);
    av_freep(&frame);
    return ret;
}

int64_t rescale_rnd_intmax(int64_t a, int64_t b, int64_t c, enum AVRounding rnd,int intmax){
    int64_t r=0;
//    av_assert2(c > 0);
//    av_assert2(b >=0);
//    av_assert2((unsigned)(rnd&~AV_ROUND_PASS_MINMAX)<=5 && (rnd&~AV_ROUND_PASS_MINMAX)!=4);

    if (rnd & AV_ROUND_PASS_MINMAX) {//false
        if (a == INT64_MIN || a == INT64_MAX)return a;
        rnd -= AV_ROUND_PASS_MINMAX;
    }
    if(a<0 && a != INT64_MIN) {//false
    	return -av_rescale_rnd(-a, b, c, rnd ^ ((rnd>>1)&1));
    }

    if(rnd==AV_ROUND_NEAR_INF) r= c/2;
    else if(rnd&1)             r= c-1;

    if(b <= intmax && c <= intmax){
        if(a<=intmax)
            return (a * b + r)/c;
        else
            return a/c*b + (a%c*b + r)/c;
    }else{
        uint64_t a0= a&0xFFFFFFFF;
        uint64_t a1= a>>32;
        uint64_t b0= b&0xFFFFFFFF;
        uint64_t b1= b>>32;
        uint64_t t1= a0*b1 + a1*b0;
        uint64_t t1a= t1<<32;
        int i;

        a0 = a0*b0 + t1a;
        a1 = a1*b1 + (t1>>32) + (a0<t1a);
        a0 += r;
        a1 += a0<r;

        for(i=63; i>=0; i--){
//            int o= a1 & 0x8000000000000000ULL;
            a1+= a1 + ((a0>>i)&1);
            t1+=t1;
            if(/*o || */c <= a1){
                a1 -= c;
                t1++;
            }
        }
        return t1;
    }
}

/**
 * Rescale from sample rate to AVCodecContext.time_base.
 */
static inline int64_t ff_samples_to_time_base(AVCodecContext *avctx,
                                                        int64_t samples)
{
    if(samples == AV_NOPTS_VALUE)
        return AV_NOPTS_VALUE;
    return av_rescale_q_rnd(samples, (AVRational){ 1, avctx->sample_rate },
                        avctx->time_base,AV_ROUND_NEAR_INF);
}

//avcodec_encode_video2(avctx, outbuf, picture, picture_buf)で呼ばれる
int inline avcodec_encode_video2(AVCodecContext *avctx,
                                               AVPacket *avpkt,
                                               const AVFrame *frame,
                                               int *got_output)
 {
// 	LOGD("avcodec_encode_video2 Called\n");
	 int ret;
	    AVPacket user_pkt = *avpkt;
	    int needs_realloc = !user_pkt.data;

	    *got_output = 0;

//	    if(avctx->internal->frame_thread_encoder && (avctx->active_thread_type&FF_THREAD_FRAME)){
//	    	LOGD("RETURN_FIRST_IF\n");//呼ばれてないっぽい
//	        return ff_thread_video_encode_frame(avctx, avpkt, frame, got_output);
//	    }
//	    if ((avctx->flags&CODEC_FLAG_PASS1) && avctx->stats_out)//常に呼ばれてない
//	        avctx->stats_out[0] = '\0';


	    if (!(avctx->codec->capabilities & CODEC_CAP_DELAY) && !frame) {//encode_video2の前に持って行った
	    	//capabilitiesの時に呼ばれるから結構呼ばれている
	        av_free_packet(avpkt);
	        av_init_packet(avpkt);
	        avpkt->size = 0;
	        return 0;
	    }

	 	LOGD("avcodec_encode_video2 B\n");
	    ret = avctx->codec->encode2(avctx, avpkt, frame, got_output);

	 	LOGD("avcodec_encode_video2 C user_pkt:%d pkt->size:%d\n",user_pkt.size,avpkt->size);
	    if (avpkt->data && avpkt->data == avctx->internal->byte_buffer) {
	        needs_realloc = 0;
	        if (user_pkt.data) {
	        	LOGD("user_pkt.size:%d avpkt->size:%d",user_pkt.size,avpkt->size);
	            if (user_pkt.size >= avpkt->size) {
	                memcpy(user_pkt.data, avpkt->data, avpkt->size);
	            } else {
	                LOGD(  "avcodec_encode_video2 Provided packet is too small, needs to be %d\n", avpkt->size);
	                avpkt->size = user_pkt.size;
	                ret = -1;
	            }
	            avpkt->data     = user_pkt.data;
	            avpkt->destruct = user_pkt.destruct;
	        } else {
	            if (av_dup_packet(avpkt) < 0) {
	                ret = -1;
	            }
	        }
	    }

	    if (!ret) {
	        if (!*got_output)
	            avpkt->size = 0;
	        else if (!(avctx->codec->capabilities & CODEC_CAP_DELAY))
	            avpkt->pts = avpkt->dts = frame->pts;

	        if (needs_realloc && avpkt->data &&
	            avpkt->destruct == av_destruct_packet) {
	            uint8_t *new_data = (uint8_t*)av_realloc(avpkt->data, avpkt->size + FF_INPUT_BUFFER_PADDING_SIZE);
	            if (new_data)
	                avpkt->data = new_data;
	        }

	    }

        avctx->frame_number++;
        LOGD("FRAME_NUM:%d",avctx->frame_number);
	    if (ret < 0 || !*got_output){
	    	av_free_packet(avpkt);
	    }else{
		    //PTSを取得する
	    	avpkt->pts = TS2T(avctx->frame_number-1, avctx->time_base)*1000;
	    }

//	    LOGD("avcodec_encode_video PTS:%f\n",TS2T(avctx->frame_number-1, avctx->time_base));
	 	LOGD("avcodec_encode_video2 END ret:%d output_size:%d avpkt->pts:%d\n",ret,avpkt->size,avpkt->pts);
	    return ret;
 }

int avcodec_encode_audio2(AVCodecContext *avctx,
                                              AVPacket *avpkt,
                                              const AVFrame *frame,
                                              int *got_packet_ptr)
{
	LOGD("avcodec_encode_audio2 Called\n");
    AVFrame *padded_frame = NULL;
    int ret;
    AVPacket user_pkt = *avpkt;
    int needs_realloc = !user_pkt.data;
    *got_packet_ptr = 0;

//    if (!(avctx->codec->capabilities & CODEC_CAP_DELAY) && !frame) {//呼ばれてないっぽい
//    	LOGD("Non Free\n");
//        av_free_packet(avpkt);
//        av_init_packet(avpkt);
//        return 0;
//    }

//    if (frame && !frame->extended_data) {//通ってなかった
//    	LOGD("av_sample_fmt_is_planar(avctx->sample_fmt):%d\n",(av_sample_fmt_is_planar(avctx->sample_fmt)));
//        if (av_sample_fmt_is_planar(avctx->sample_fmt) &&
//            avctx->channels > AV_NUM_DATA_POINTERS) {
//            LOGD( "Encoding to a planar sample format, "
//                                        "with more than %d channels, but extended_data is not set.\n",
//                   AV_NUM_DATA_POINTERS);
//            return -1;
//        }
//        tmp = *frame;
//        tmp.extended_data = tmp.data;
//        frame = &tmp;
//    }

    /* check for valid frame size */
    if (frame) {
//        if (avctx->codec->capabilities & CODEC_CAP_SMALL_LAST_FRAME) {//成功するときは呼ばれてないはず
//            if (frame->nb_samples > avctx->frame_size) {
//                return -1;
//            	LOGD( "more samples than frame size (avcodec_encode_audio2)\n");
//            }
//        } else
        	if (!(avctx->codec->capabilities & CODEC_CAP_VARIABLE_FRAME_SIZE)) {
            if (frame->nb_samples < avctx->frame_size &&
                !avctx->internal->last_audio_frame) {
                ret = pad_last_frame(avctx, &padded_frame, frame);
                if (ret < 0)
                    return ret;
                frame = padded_frame;
                avctx->internal->last_audio_frame = 1;
            }

            if (frame->nb_samples != avctx->frame_size) {
            	LOGD( "nb_samples (%d) != frame_size (%d) (avcodec_encode_audio2)\n", frame->nb_samples, avctx->frame_size);
                ret = -1;
                goto end;
            }
        }
    }

    ret = avctx->codec->encode2(avctx, avpkt, frame, got_packet_ptr);

//    if (!ret) {
//        if (*got_packet_ptr) {
//            if (!(avctx->codec->capabilities & CODEC_CAP_DELAY)) {
//                if (avpkt->pts == AV_NOPTS_VALUE)
//                    avpkt->pts = frame->pts;
//                if (!avpkt->duration)
//                    avpkt->duration = ff_samples_to_time_base(avctx,
//                                                              frame->nb_samples);
//            }
//            avpkt->dts = avpkt->pts;
//        } else {
//            avpkt->size = 0;
//        }
//    }
    if (avpkt->data && avpkt->data == avctx->internal->byte_buffer) {
        needs_realloc = 0;
        if (user_pkt.data) {
            if (user_pkt.size >= avpkt->size) {
                memcpy(user_pkt.data, avpkt->data, avpkt->size);
            } else {
            	LOGD("at avcodec_encode_audio2 Provided packet is too small, needs to be %d\n", avpkt->size);
                avpkt->size = user_pkt.size;
                ret = -1;
            }
            avpkt->data     = user_pkt.data;
            avpkt->destruct = user_pkt.destruct;
        } else {
            if (av_dup_packet(avpkt) < 0) {
                ret = -1;
            }
        }
    }

    if (!ret) {
        if (needs_realloc && avpkt->data) {
            uint8_t *new_data = (uint8_t*)av_realloc(avpkt->data, avpkt->size + FF_INPUT_BUFFER_PADDING_SIZE);
            if (new_data)
                avpkt->data = new_data;
        }

        avctx->frame_number++;
    }

    if (ret < 0 || !*got_packet_ptr) {
        av_free_packet(avpkt);
        av_init_packet(avpkt);
        goto end;
    }

    /* NOTE: if we add any audio encoders which output non-keyframe packets,
     *       this needs to be moved to the encoders, but for now we can do it
     *       here to simplify things */
//    avpkt->flags |= AV_PKT_FLAG_KEY;

end:
    if (padded_frame) {
        av_freep(&padded_frame->data[0]);
        if (padded_frame->extended_data != padded_frame->data)
            av_freep(&padded_frame->extended_data);
        av_freep(&padded_frame);
    }

    return ret;
}



