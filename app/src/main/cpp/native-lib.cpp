#include <jni.h>
#include "opencv2/opencv.hpp"
#include <android/log.h>

using namespace cv;

#define LOG_TAG "FaceDetection"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

DetectionBasedTracker *tracker;

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
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    //yuv的宽高计算方式
    //src->bitmap
    Mat src(height + height / 2, width, CV_8UC1, data);
    //nv21->rgba
    cvtColor(src, src, COLOR_YUV2RGBA_NV21);
    //输出图片
    imwrite("/sdcard/src.jpg", src);
    LOGD("postData call success");
    env->ReleaseByteArrayElements(data_, data, 0);
}