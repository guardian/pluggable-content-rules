import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { PluggableContentRules } from "./google-search-indexing-observatory";

describe("The PluggableContentRules stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new PluggableContentRules(app, "PluggableContentRules", { stack: "ophan", stage: "TEST" });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});
