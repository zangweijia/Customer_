package com.bopinjia.customer.qrcode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityHome;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.qrcode.camera.CameraManager;
import com.bopinjia.customer.qrcode.camera.PlanarYUVLuminanceSource;
import com.bopinjia.customer.qrcode.decoding.CaptureActivityHandler;
import com.bopinjia.customer.qrcode.decoding.InactivityTimer;
import com.bopinjia.customer.qrcode.decoding.RGBLuminanceSource;
import com.bopinjia.customer.qrcode.view.ViewfinderView;
import com.bopinjia.customer.util.ImageGet;
import com.bopinjia.customer.util.StorageUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;


/**
 * @author Administrator
 * 扫码界面
 */
public class CaptureActivity extends Activity implements Callback {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);
        // CameraManager
        CameraManager.init(getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

        findViewById(R.id.btn_return).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
        		finish();
            }
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    public void handleDecode(Result obj, Bitmap barcode) {
        inactivityTimer.onActivity();
        // viewfinderView.drawResultBitmap(barcode);
        playBeepSoundAndVibrate();

        Intent intent = new Intent();
        intent.putExtra(Constants.INTENT_EXTRA_SCAN_RESULT, obj.getText());

        setResult(RESULT_OK, intent);
        CaptureActivity.this.finish();
        
        
//        String resultString = obj.getText();
//		// FIXME
//		if (resultString.equals("")) {
//			Toast.makeText(CaptureActivity.this, "扫描失败!", Toast.LENGTH_SHORT)
//					.show();
//		} else {
//			Intent resultIntent = new Intent();
//			Bundle bundle = new Bundle();
//			Matrix matrix = new Matrix();
//			matrix.postScale(0.5f, 0.5f);
//			Bitmap bit = Bitmap.createBitmap(barcode, 0, 0, barcode.getWidth(),
//					barcode.getHeight(), matrix, true);
//			bundle.putString(Constants.INTENT_EXTRA_SCAN_RESULT, resultString);
//			bundle.putParcelable("bitmap", bit);
//			resultIntent.putExtras(bundle);
//			this.setResult(RESULT_OK, resultIntent);
//		}
//		CaptureActivity.this.finish();
        
        
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
    
    //2016.7.7修改
    ProgressDialog mProgress;
    String photo_path;
	Bitmap scanBitmap;
    
	/* 2016.7.7修改
	 * 获取带二维码的相片进行扫描
	 */
	public void pickPictureFromAblum(View v) {
		// 打开手机中的相册
		Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
		innerIntent.setType("image/*");
		Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
		this.startActivityForResult(wrapperIntent, 1);
	}
	/*2016.7.7修改
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent) 对相册获取的结果进行分析
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1:
				try {
					Uri uri = data.getData();
					if (!TextUtils.isEmpty(uri.getAuthority())) {
						Cursor cursor = getContentResolver().query(uri,
								new String[] { MediaStore.Images.Media.DATA },
								null, null, null);
						if (null == cursor) {
							Toast.makeText(this, "图片没找到", Toast.LENGTH_SHORT)
									.show();
							return;
						}
						cursor.moveToFirst();
//						photo_path = cursor.getString(cursor
//								.getColumnIndex(MediaStore.Images.Media.DATA));
						photo_path = ImageGet.getPath(this, uri);
						
						cursor.close();
					} else {
						photo_path = data.getData().getPath();
					}
					mProgress = new ProgressDialog(CaptureActivity.this);
					mProgress.setMessage("正在扫描...");
					mProgress.setCancelable(false);
					mProgress.show();

					new Thread(new Runnable() {
						@Override
						public void run() {
							Result result = scanningImage(photo_path);
							if (result != null) {
								Message m = mHandler.obtainMessage();
								m.what = 1;
								m.obj = result.getText();

								mHandler.sendMessage(m);
							} else {
								Message m = mHandler.obtainMessage();
								m.what = 2;
								m.obj = "Scan failed!";
								mHandler.sendMessage(m);
							}

						}
					}).start();
				} catch (Exception e) {
					Toast.makeText(CaptureActivity.this, "解析错误！",
							Toast.LENGTH_LONG).show();
				}

				break;

			default:
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			switch (msg.what) {
			case 1:
				mProgress.dismiss();
				String resultString = msg.obj.toString();
				if (resultString.equals("")) {
					Toast.makeText(CaptureActivity.this, "扫描失败!",
							Toast.LENGTH_SHORT).show();
				} else {
					// System.out.println("Result:"+resultString);
					Intent resultIntent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString(Constants.INTENT_EXTRA_SCAN_RESULT, resultString);
					resultIntent.putExtras(bundle);
					CaptureActivity.this.setResult(RESULT_OK, resultIntent);
				}
				CaptureActivity.this.finish();
				break;

			case 2:
				mProgress.dismiss();
				Toast.makeText(CaptureActivity.this, "请选取正确二维码！", Toast.LENGTH_LONG)
						.show();

				break;
			default:
				break;
			}

			super.handleMessage(msg);
		}

	};
//	/**
//	 * 2016.7.7修改
//	 * 扫描二维码图片的方法
//	 * 
//	 * 目前识别度不高，有待改进
//	 * 
//	 * @param path
//	 * @return
//	 */
//	public Result scanningImage(String path) {
//		if (TextUtils.isEmpty(path)) {
//			return null;
//		}
//		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
//		hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); // 设置二维码内容的编码
//
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = true; // 先获取原大小
//		scanBitmap = BitmapFactory.decodeFile(path, options);
//		options.inJustDecodeBounds = false; // 获取新的大小
//		int sampleSize = (int) (options.outHeight / (float) 100);
//		if (sampleSize <= 0)
//			sampleSize = 1;
//		options.inSampleSize = sampleSize;
//		scanBitmap = BitmapFactory.decodeFile(path, options);
//		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
//		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
//		QRCodeReader reader = new QRCodeReader();
//		try {
//			return reader.decode(bitmap1, hints);
//
//		} catch (NotFoundException e) {
//			e.printStackTrace();
//		} catch (ChecksumException e) {
//			e.printStackTrace();
//		} catch (FormatException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	// TODO: 解析部分图片
			protected Result scanningImage(String path) {
				if (TextUtils.isEmpty(path)) {

					return null;

				}
				// DecodeHintType 和EncodeHintType
				Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
				hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true; // 先获取原大小
				scanBitmap = BitmapFactory.decodeFile(path, options);
				options.inJustDecodeBounds = false; // 获取新的大小

				int sampleSize = (int) (options.outHeight / (float) 200);

				if (sampleSize <= 0)
					sampleSize = 1;
				options.inSampleSize = sampleSize;
				scanBitmap = BitmapFactory.decodeFile(path, options);

				// --------------测试的解析方法---PlanarYUVLuminanceSource-这几行代码对project没作功----------

				LuminanceSource source1 = new PlanarYUVLuminanceSource(
						rgb2YUV(scanBitmap), scanBitmap.getWidth(),
						scanBitmap.getHeight(), 0, 0, scanBitmap.getWidth(),
						scanBitmap.getHeight());
				BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
						source1));
				MultiFormatReader reader1 = new MultiFormatReader();
				Result result1;
				try {
					result1 = reader1.decode(binaryBitmap);
					String content = result1.getText();
					Log.i("123content", content);
				} catch (NotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// ----------------------------

				RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
				BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
				QRCodeReader reader = new QRCodeReader();
				try {

					return reader.decode(bitmap1, hints);

				} catch (NotFoundException e) {

					e.printStackTrace();

				} catch (ChecksumException e) {

					e.printStackTrace();

				} catch (FormatException e) {

					e.printStackTrace();

				}

				return null;

			}

		
			/**
			 * 中文乱码
			 * 
			 * 暂时解决大部分的中文乱码 但是还有部分的乱码无法解决 .		
			 * @return
			 */
			private String recode(String str) {
				String formart = "";

				try {
					boolean ISO = Charset.forName("ISO-8859-1").newEncoder()
							.canEncode(str);
					if (ISO) {
						formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
						Log.i("1234      ISO8859-1", formart);
					} else {
						formart = str;
						Log.i("1234      stringExtra", str);
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return formart;
			}	
		
			/**
			 * //TODO: TAOTAO 将bitmap由RGB转换为YUV //TOOD: 研究中
			 * 
			 * @param bitmap
			 *            转换的图形
			 * @return YUV数据
			 */
			public byte[] rgb2YUV(Bitmap bitmap) {
				// 该方法来自QQ空间
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				int[] pixels = new int[width * height];
				bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

				int len = width * height;
				byte[] yuv = new byte[len * 3 / 2];
				int y, u, v;
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						int rgb = pixels[i * width + j] & 0x00FFFFFF;

						int r = rgb & 0xFF;
						int g = (rgb >> 8) & 0xFF;
						int b = (rgb >> 16) & 0xFF;

						y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
						u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
						v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

						y = y < 16 ? 16 : (y > 255 ? 255 : y);
						u = u < 0 ? 0 : (u > 255 ? 255 : u);
						v = v < 0 ? 0 : (v > 255 ? 255 : v);

						yuv[i * width + j] = (byte) y;
						// yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
						// yuv[len + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
					}
				}
				return yuv;
			}
		
		
}