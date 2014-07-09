
#include <math.h>
#include <stdint.h>

#define TS2D(ts) ((ts) == AV_NOPTS_VALUE ? 0 : (double)(ts))
#define TS2T(ts, tb) ((ts) == AV_NOPTS_VALUE ? NAN : (double)(ts)*av_q2d(tb))

#define BUF_SIZE 64
//static inline char *double2int64str(char *buf, double v)
//{
//    if (isnan(v)){
//    	snprintf(buf, BUF_SIZE, "nan");
//    }else{
//    	snprintf(buf, BUF_SIZE, "%d", (int64_t)v);
//    }
//    return buf;
//}
#define d2istr(v) double2int64str((char[BUF_SIZE]){0}, v)

int set_linesize(int linesize_[],int width);
int set_video_pts(int frame_number,AVRational frame_rate);
int avpicture_fill(uint8_t *dst_data[4], int dst_linesize[4],
                         const uint8_t *src,
                         enum AVPixelFormat pix_fmt, int width, int height, int align);
int get_linesizes(int linesize_[4],int width_,enum AVPixelFormat  pix_fmt);
