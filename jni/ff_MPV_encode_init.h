

int ff_MPV_encode_init(AVCodecContext *avctx);
int ff_MPV_encode_picture(AVCodecContext *avctx, AVPacket *pkt,
                          AVFrame *pic_arg, int *got_packet);
av_cold int ff_MPV_encode_end(AVCodecContext *avctx);

int inline avcodec_encode_video2(AVCodecContext *avctx,
                                               AVPacket *avpkt,
                                               const AVFrame *frame,
                                               int *got_packet_ptr);
