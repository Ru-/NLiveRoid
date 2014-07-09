/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel;

import org.jboss.netty.channel.socket.nio.NioSocketChannel;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * The default {@link ExceptionEvent} implementation.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 *
 */
public class DefaultExceptionEvent implements ExceptionEvent {

    private static Context context;
	private final NioSocketChannel channel;
    private final Throwable cause;

    public static void setUIContext(Context cont){
    	context = cont;
    }
    /**
     * Creates a new instance.
     */
    public DefaultExceptionEvent(NioSocketChannel channel, Throwable cause) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        this.channel = channel;
        this.cause = cause;

        Log.d("DefaultExceptionEvent!! ","CHANNEL " + channel);
        cause.printStackTrace();
        final String causeStr = cause.getMessage();
        new ToastTask().execute(causeStr);
//        StackTraceSimplifier.simplify(cause);
    }

    class ToastTask extends AsyncTask<String,Void,String>{

		@Override
		protected String doInBackground(String... params) {
			return params[0];
		}

		@Override
		protected void onPostExecute(String arg){
	        	Toast.makeText(context,"不明のエラー" , Toast.LENGTH_LONG).show();
		}

    }
    public NioSocketChannel getChannel() {
        return channel;
    }

    public ChannelFuture getFuture() {
        return new SucceededChannelFuture(getChannel());
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return getChannel().toString() + " EXCEPTION: " + cause;
    }
}
