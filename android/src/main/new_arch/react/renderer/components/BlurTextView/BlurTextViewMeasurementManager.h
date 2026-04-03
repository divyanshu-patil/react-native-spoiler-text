#pragma once

#include <react/renderer/components/BlurTextViewSpec/Props.h>
#include <react/renderer/core/LayoutConstraints.h>
#include <react/utils/ContextContainer.h>

namespace facebook::react {

class BlurTextViewMeasurementManager {
public:
  BlurTextViewMeasurementManager(
      const std::shared_ptr<const ContextContainer> &contextContainer)
      : contextContainer_(contextContainer) {}

  Size measure(SurfaceId surfaceId, int viewTag, const BlurTextViewProps &props,
               LayoutConstraints layoutConstraints) const;

  float measureSingleLineHeight(const BlurTextViewProps &props) const;

private:
  const std::shared_ptr<const ContextContainer> contextContainer_;
};

} // namespace facebook::react
