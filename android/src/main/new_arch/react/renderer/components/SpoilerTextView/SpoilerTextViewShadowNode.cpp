#include "SpoilerTextViewShadowNode.h"

#include "conversions.h"
#include <android/log.h>
#include <folly/json.h>
#include <react/renderer/core/LayoutContext.h>

namespace facebook::react {

extern const char SpoilerTextViewComponentName[] = "SpoilerTextView";

void SpoilerTextViewShadowNode::setMeasurementsManager(
    const std::shared_ptr<SpoilerTextViewMeasurementManager>
        &measurementsManager) {
  ensureUnsealed();
  measurementsManager_ = measurementsManager;
}

void SpoilerTextViewShadowNode::dirtyLayoutIfNeeded() {
  const auto state = this->getStateData();
  const auto counter = state.getForceHeightRecalculationCounter();

  if (forceHeightRecalculationCounter_ != counter) {
    forceHeightRecalculationCounter_ = counter;
    dirtyLayout();
  }
}

Size SpoilerTextViewShadowNode::measureContent(
    const LayoutContext &layoutContext,
    const LayoutConstraints &layoutConstraints) const {

  const auto &props = getConcreteProps();

  // Debug props (optional)
  try {
    folly::dynamic dyn = toDynamic(props);
    std::string json = folly::toJson(dyn);
    __android_log_print(ANDROID_LOG_INFO, "SpoilerText", "props = %s",
                        json.c_str());
  } catch (const std::exception &e) {
    __android_log_print(ANDROID_LOG_ERROR, "SpoilerText", "toDynamic() error: %s",
                        e.what());
  }

  // Just delegate to measurement manager
  auto size = measurementsManager_->measure(getSurfaceId(), getTag(), props,
                                            layoutConstraints);

  return size;
}

} // namespace facebook::react
