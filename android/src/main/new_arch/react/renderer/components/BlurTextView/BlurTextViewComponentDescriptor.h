#pragma once

#include "BlurTextViewMeasurementManager.h"
#include "BlurTextViewShadowNode.h"

#include <react/renderer/core/ConcreteComponentDescriptor.h>

namespace facebook::react {

class BlurTextViewComponentDescriptor final
    : public ConcreteComponentDescriptor<BlurTextViewShadowNode> {
public:
  BlurTextViewComponentDescriptor(
      const ComponentDescriptorParameters &parameters)
      : ConcreteComponentDescriptor(parameters),
        measurementsManager_(std::make_shared<BlurTextViewMeasurementManager>(
            contextContainer_)) {}

  void adopt(ShadowNode &shadowNode) const override {
    ConcreteComponentDescriptor::adopt(shadowNode);
    auto &editorShadowNode = static_cast<BlurTextViewShadowNode &>(shadowNode);

    // `BlurTextViewShadowNode` uses
    // `BlurTextViewMeasurementManager` to provide measurements to
    // Yoga.
    editorShadowNode.setMeasurementsManager(measurementsManager_);
  }

private:
  const std::shared_ptr<BlurTextViewMeasurementManager> measurementsManager_;
};

} // namespace facebook::react
