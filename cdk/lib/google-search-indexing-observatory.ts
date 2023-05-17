import type { GuStackProps } from "@guardian/cdk/lib/constructs/core";
import { GuStack } from "@guardian/cdk/lib/constructs/core";
import type { App } from "aws-cdk-lib";
import {GuApiLambda, GuScheduledLambda} from "@guardian/cdk";
import {Runtime} from "aws-cdk-lib/aws-lambda";
import {Schedule} from "aws-cdk-lib/aws-events";
import {Duration} from "aws-cdk-lib";
import {NoMonitoring} from "@guardian/cdk/lib/constructs/cloudwatch";
import {AttributeType, Table} from "aws-cdk-lib/aws-dynamodb";

export class PluggableContentRules extends GuStack {
  constructor(scope: App, id: string, props: GuStackProps) {
    super(scope, id, props);
    const noMonitoring: NoMonitoring = { noMonitoring: true };
    const apiLambda = new GuApiLambda(this, "my-lambda", {
      fileName: "pluggable-content-rules.jar",
      handler: "com.theguardian.content.rules.Lambda::handler",
      runtime: Runtime.JAVA_11,
      monitoringConfiguration: noMonitoring,
      app: "pluggable-content-rules",
      api: {
        id: "pluggable-content-rules",
        description: "Pluggable content rules API",
      },
    });
  }
}
