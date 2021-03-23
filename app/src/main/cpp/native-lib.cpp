#include <jni.h>
#include "opencv2/opencv.hpp"
#include <android/log.h>
#include <android//native_window_jni.h>

using namespace cv;

#define LOG_TAG "FaceDetection"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

DetectionBasedTracker *tracker;
ANativeWindow *window = nullptr;

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {
        LOGD("CascadeDetectorAdapter::Detect::Detect");
        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {
        LOGD("CascadeDetectorAdapter::Detect: begin");
        LOGD("CascadeDetectorAdapter::Detect: scaleFactor=%.2f, minNeighbours=%d, minObjSize=(%dx%d), maxObjSize=(%dx%d)",
             scaleFactor, minNeighbours, minObjSize.width, minObjSize.height, maxObjSize.width,
             maxObjSize.height);
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                   maxObjSize);
        LOGD("CascadeDetectorAdapter::Detect: end");
    }

    virtual ~CascadeDetectorAdapter() {
        LOGD("CascadeDetectorAdapter::Detect::~Detect");
    }

private:
    CascadeDetectorAdapter();

    cv::Ptr<cv::CascadeClassifier> Detector;
};

extern "C"
JNIEXPORT void JNICALL
Java_com_kuyu_opencvdemo_jni_JniManager_init(JNIEnv *env, jobject instance, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    LOGD("init call");
    //创建智能指针
    Ptr<CascadeClassifier> classifier = makePtr<CascadeClassifier>(path);
    //创建检测器
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(classifier);

    //跟踪器
    //智能指针
    Ptr<CascadeClassifier> classifier1 = makePtr<CascadeClassifier>(path);
    //创建检测器
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(classifier1);

    DetectionBasedTracker::Parameters parameters;
    tracker = new DetectionBasedTracker(mainDetector, trackingDetector, parameters);
    tracker->run();

    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_kuyu_opencvdemo_jni_JniManager_postData(JNIEnv *env, jobject instance, jbyteArray data_,
                                                 jint width, jint height, jint cameraId) {
    //NV21数据->Bitmap->Mat
    jbyte *data = env->GetByteArrayElements(data_, nullptr);
    //yuv的宽高计算方式
    //src->bitmap
    Mat src(height + height / 2, width, CV_8UC1, data);
    //nv21->rgba
    cvtColor(src, src, COLOR_YUV2RGBA_NV21);
    //输出图片
    imwrite("/data/user/0/com.kuyu.opencvdemo/files/src.jpg", src);
    LOGD("postData call success");
    //0:后置摄像头，1:前置摄像头
    if (cameraId == 1) {
        //逆时针旋转
//        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);
        //翻转：1水平翻转，0垂直翻转
        flip(src, src, 1);
    } else {
        rotate(src, src, ROTATE_90_CLOCKWISE);
    }
    //人脸识别,转成灰度图
    Mat gray;
    cvtColor(src, gray, COLOR_RGBA2GRAY);
    imwrite("/data/user/0/com.kuyu.opencvdemo/files/src4.jpg", gray);
    //增强对比度，增强轮廓，二值化
    equalizeHist(gray, gray);
    //检测人脸
    std::vector<Rect> faces;
    tracker->process(gray);
    tracker->getObjects(faces);
    for (Rect face:  faces) {
        rectangle(src, face, Scalar(255, 0, 255));
        LOGD("detect face already");
    }
    if (window) {
        int32_t result = ANativeWindow_setBuffersGeometry(window, src.cols, src.rows,WINDOW_FORMAT_RGBA_8888);
        if (result < 0){
            ANativeWindow_release(window);
            window = nullptr;
            return;
        }
        ANativeWindow_Buffer windowBuffer;
        do {
            //lock失败直接break出去
            if (ANativeWindow_lock(window, &windowBuffer, 0) < 0) {
                ANativeWindow_release(window);
                window = nullptr;
                break;
            }
            LOGD("window is not nullptr : post data");
            //src.data: rgba的数据拷贝到buffer.bits中，一行一行的拷贝
            //填充rgb数据给dst_data
            uint8_t *dst_data = static_cast<uint8_t *>(windowBuffer.bits);
            //stride: 一行多少个数据（RGBA）*4
            int dst_line_size = windowBuffer.stride * 4;
            //一行一行的拷贝
            for (int i = 0; i < windowBuffer.height; ++i) {
                memcpy(dst_data + i * dst_line_size, src.data + i * src.cols * 4, dst_line_size);
            }
            //提交刷新
            ANativeWindow_unlockAndPost(window);
        } while (0);
    }
    src.release();
    gray.release();
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_kuyu_opencvdemo_jni_JniManager_setSurface(JNIEnv *env, jobject thiz, jobject surface) {
    if (window) {
        ANativeWindow_release(window);
        window = nullptr;
    }
    LOGD("call setSurface true");
    window = ANativeWindow_fromSurface(env, surface);
    if (window) {
        LOGD("window is not nullptr");
    } else {
        LOGD("window is nullptr");
    }
}