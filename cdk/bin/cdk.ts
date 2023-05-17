import "source-map-support/register";
import { App } from "aws-cdk-lib";
import { PluggableContentRules } from "../lib/google-search-indexing-observatory";
import {GuRootExperimental} from "@guardian/cdk/lib/experimental/constructs/root";

const app: App = new GuRootExperimental();
new PluggableContentRules(app, "PluggableContentRules-PROD", { stack: "playground", stage: "PROD", env: {region: "eu-west-1"} });
