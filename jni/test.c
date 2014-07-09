
#include <stdio.h>
#include <lame/lame.h>

int test_mono() {

  unsigned int sampleRate = 44100;  /*assumed.*/
  int PCM_SIZE = 1152;
  int write_size = 1;
  int read_size = 2;

  lame_global_flags *gfp;
  int mp3buffer[PCM_SIZE*2]; /*some odd buffer sizes hard-coded.*/
  short pcm_samples_1d[2*PCM_SIZE];
  short pcm_samples_2d[2][PCM_SIZE];
  int read = 0, write = 0;
  FILE *output;
  FILE *pcm;
  int framesize = 0;
  int i = 0, j = 0, num_samples_encoded = 0;

  /*Step 1. Generate sinusoid.*/
  /*arr = (float *) malloc(sizeof(float) * nSecondsAudio * sampleRate);
  arr = generateSinusoid(sampleRate, nSecondsAudio);*/

  /*Step 2. See if encoder exists.*/
  char *s = (char *) malloc(sizeof(char)*200);
  s = get_lame_version();
  printf("Lame version = %s\n", s);


  /* Init lame flags.*/
  gfp = lame_init();
  if(!gfp) {
    printf("Unable to initialize gfp object.");
  } else {
    printf("Able to initialize gfp object.\n");
  }

  /* set other parameters.*/
  lame_set_num_channels(gfp, 1);
  /*lame_set_num_samples(gfp, (nSecondsAudio * sampleRate));*/
  lame_set_in_samplerate(gfp, sampleRate);
  lame_set_quality(gfp, 2);  /* set for high speed and good quality. */
  lame_set_mode(gfp, MONO);  /* the input audio is mono */

  lame_set_out_samplerate(gfp, sampleRate);
  printf("Able to set a number of parameters too.");
  framesize = lame_get_framesize(gfp);
  printf("Framesize = %d\n", framesize);

  /* set more internal variables. check for failure.*/
  if(lame_init_params(gfp) == -1) {
    printf("Something failed in setting internal parameters.");
  }

  /* encode the pcm array as mp3.*
   * Read the file. Encode whatever is read.
   * As soon as end of file is reached, flush the buffers.
   * Write everything to a file.
   * Write headers too.
  */

  /* Open PCM file for reading from.*/
  pcm = fopen("testPCM.pcm", "rb");   /*hard-coded to the only available pcm file.*/
  if(!pcm) {
    printf("Cannot open pcm file for reading.");
    return 1;
  }

  output = fopen("out.mp3", "wb+");
  if(!output) {
    printf("Cannot open file for writing.");
    return 1;
 }

  do {
   read = fread(pcm_samples_1d, sizeof(short), PCM_SIZE*read_size, pcm); /*reads framesize shorts from pcm file.*/
   printf("Read %d shorts from file.\n", read);

   /* check for number of samples read. if 0, start flushing, else encode.*/
   if(read > 0) {
     /* got data in 1D array. convert it to 2D */
     /* snippet below taken from lame source code. needs better understanding. pcm_samples_2d[0] = contents of buffer. pcm_samples_2d[1] = 0 since number of channels is always one.*/
     memset(pcm_samples_2d[1], 0, PCM_SIZE * read_size);  /*set all other samples with 0.*/
     memset(pcm_samples_2d[0], 0, PCM_SIZE * read_size);
     i = 0, j = 0;
     for(i = 0; i < PCM_SIZE; i++) {
       pcm_samples_2d[0][i] = pcm_samples_1d[i] ;
       pcm_samples_2d[1][i] = pcm_samples_1d[i] ;
     }

     /* encode samples. */
     num_samples_encoded = lame_encode_buffer(gfp, pcm_samples_2d[0], pcm_samples_2d[1], read, mp3buffer, sizeof(mp3buffer));

     printf("number of samples encoded = %d\n", num_samples_encoded);

     /* check for value returned.*/
     if(num_samples_encoded > 1) {
       printf("It seems the conversion was successful.\n");
     } else if(num_samples_encoded == -1) {
       printf("mp3buf was too small");
       return 1;
     } else if(num_samples_encoded == -2) {
       printf("There was a malloc problem.");
       return 1;
     } else if(num_samples_encoded == -3) {
       printf("lame_init_params() not called.");
       return 1;
     } else if(num_samples_encoded == -4) {
       printf("Psycho acoustic problems.");
       return 1;
     } else {
       printf("The conversion was not successful.");
       return 1;
     }

//     printf("Contents of mp3buffer = \n");
//     for(i = 0; i < 2304; i++) {
//       printf("mp3buffer[%d] = %d\n", i, mp3buffer[i]);
//     }


     write = (int) fwrite(mp3buffer, write_size, num_samples_encoded, output);
     if(write != num_samples_encoded) {
       printf("There seems to have been an error writing to mp3 within the loop.\n");
       return 1;
     } else {
       printf("Writing of %d samples a success.\n", write);
     }
   }
//   LOGD("debug BSWAP32_RN(s->buffer):%d\n",BSWAP32_RN(s->buffer));
 } while(read > 0);

 /* in case where the number of samples read is 0, or negative, start flushing.*/
 read = lame_encode_flush(gfp, mp3buffer, sizeof(mp3buffer)); /*this may yield one more mp3 buffer.*/
 if(read < 0) {
   if(read == -1) {
     printf("mp3buffer is probably not big enough.\n");
   } else {
     printf("MP3 internal error.\n");
   }
   return 1;
 } else {
   printf("Flushing stage yielded %d frames.\n", read);
 }

 write = (int) fwrite(mp3buffer, write_size, read, output);
 if(write != read) {
   printf("There seems to have been an error writing to mp3.\n");
   return 1;
 }

  /*samples have been written. write ID3 tag.*/
  read = lame_get_id3v1_tag(gfp, mp3buffer, sizeof(mp3buffer));
  if(sizeof(read) > sizeof(mp3buffer)) {
    printf("Buffer too small to write ID3v1 tag.\n");
  } else {
    if(read > 0) {
      write = (int) fwrite(mp3buffer,write_size, read, output);
      if(read != write) {
        printf("more errors in writing id tag to mp3 file.\n");
      }
    }
  }

  lame_close(gfp);
  fclose(pcm);
  fclose(output);

  return 0;
}

int test_stereo(){
	LOGD("TEST\n");
	test_mono();
	return 0;
	int read, write;

	    FILE *pcm = fopen("aiueo.wav", "rb");
	    FILE *mp3 = fopen("file.mp3", "wb");

	    const int PCM_SIZE = 8000;
	    const int MP3_SIZE = 8000;

	    short int pcm_buffer[PCM_SIZE*2];
	    unsigned char mp3_buffer[MP3_SIZE];

	    lame_t lame = lame_init();
	    lame_set_in_samplerate(lame, 44100);
	    lame_set_VBR(lame, vbr_off);
	    lame_set_mode(lame,MONO);
	    lame_set_num_channels(lame,1);
	    lame_set_brate(lame,16);
	    lame_init_params(lame);

	    do {
	        read = fread(pcm_buffer, 2*sizeof(short int), PCM_SIZE, pcm);
			LOGD("TEST read:%d\n",read);
	        if (read == 0)
	            write = lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
	        else
	            write = lame_encode_buffer_interleaved(lame, pcm_buffer, read, mp3_buffer, MP3_SIZE);
	        fwrite(mp3_buffer, write, 1, mp3);
	    } while (read != 0);

		LOGD("TEST write:%d\n",write);
	    lame_close(lame);
	    fclose(mp3);
	    fclose(pcm);

	return 0;
}
