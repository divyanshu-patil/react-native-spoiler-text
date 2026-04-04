#pragma once

#include <react/renderer/components/SpoilerTextViewSpec/Props.h>
#include <react/renderer/core/LayoutConstraints.h>
#include <react/utils/ContextContainer.h>

namespace facebook::react {

class SpoilerTextViewMeasurementManager {
public:
  SpoilerTextViewMeasurementManager(
      const std::shared_ptr<const ContextContainer> &contextContainer)
      : contextContainer_(contextContainer) {}

  Size measure(SurfaceId surfaceId, int viewTag, const SpoilerTextViewProps &props,
               LayoutConstraints layoutConstraints) const;

  float measureSingleLineHeight(const SpoilerTextViewProps &props) const;

private:
  const std::shared_ptr<const ContextContainer> contextContainer_;
};

} // namespace facebook::react
