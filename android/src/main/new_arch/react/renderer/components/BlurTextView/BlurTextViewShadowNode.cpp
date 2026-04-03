#include "BlurTextViewShadowNode.h"

#include "conversions.h"
#include <android/log.h>
#include <folly/json.h>
#include <react/renderer/core/LayoutContext.h>

namespace facebook::react {

extern const char BlurTextViewComponentName[] = "BlurTextView";

void BlurTextViewShadowNode::setMeasurementsManager(
    const std::shared_ptr<BlurTextViewMeasurementManager>
        &measurementsManager) {
  ensureUnsealed();
  measurementsManager_ = measurementsManager;
}

void BlurTextViewShadowNode::dirtyLayoutIfNeeded() {
  const auto state = this->getStateData();
  const auto counter = state.getForceHeightRecalculationCounter();

  if (forceHeightRecalculationCounter_ != counter) {
    forceHeightRecalculationCounter_ = counter;
    dirtyLayout();
  }
}

Size BlurTextViewShadowNode::measureContent(
    const LayoutContext &layoutContext,
    const LayoutConstraints &layoutConstraints) const {

  const auto &props = getConcreteProps();

  // Debug props (optional)
  try {
    folly::dynamic dyn = toDynamic(props);
    std::string json = folly::toJson(dyn);
    __android_log_print(ANDROID_LOG_INFO, "BlurText", "props = %s",
                        json.c_str());
  } catch (const std::exception &e) {
    __android_log_print(ANDROID_LOG_ERROR, "BlurText", "toDynamic() error: %s",
                        e.what());
  }

  // Just delegate to measurement manager
  auto size = measurementsManager_->measure(getSurfaceId(), getTag(), props,
                                            layoutConstraints);

  return size;
}

} // namespace facebook::react