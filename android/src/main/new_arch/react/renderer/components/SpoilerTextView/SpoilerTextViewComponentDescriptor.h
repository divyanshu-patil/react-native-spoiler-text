#pragma once

#include "SpoilerTextViewMeasurementManager.h"
#include "SpoilerTextViewShadowNode.h"

#include <react/renderer/core/ConcreteComponentDescriptor.h>

namespace facebook::react {

class SpoilerTextViewComponentDescriptor final
    : public ConcreteComponentDescriptor<SpoilerTextViewShadowNode> {
public:
  SpoilerTextViewComponentDescriptor(
      const ComponentDescriptorParameters &parameters)
      : ConcreteComponentDescriptor(parameters),
        measurementsManager_(std::make_shared<SpoilerTextViewMeasurementManager>(
            contextContainer_)) {}

  void adopt(ShadowNode &shadowNode) const override {
    ConcreteComponentDescriptor::adopt(shadowNode);
    auto &editorShadowNode = static_cast<SpoilerTextViewShadowNode &>(shadowNode);

    // `SpoilerTextViewShadowNode` uses
    // `SpoilerTextViewMeasurementManager` to provide measurements to
    // Yoga.
    editorShadowNode.setMeasurementsManager(measurementsManager_);
  }

private:
  const std::shared_ptr<SpoilerTextViewMeasurementManager> measurementsManager_;
};

} // namespace facebook::react
