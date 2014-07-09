#ifndef ENCODE_H_
#define ENCODE_H_
void av_image_fill_max_pixsteps(int max_pixsteps[4], int max_pixstep_comps[4],
                                const AVPixFmtDescriptor *pixdesc);

int64_t rescale_rnd_intmax(int64_t a, int64_t b, int64_t c, enum AVRounding rnd,int intmax);

#endif
