import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { SurveyService } from '../../../core/services/survey.service';
import { SurveyResponse, QuestionResponse } from '../../../core/models/survey.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-survey-detail',
  standalone: true,
  imports: [DatePipe, RouterLink, PageHeaderComponent, StatusBadgeComponent, HasRoleDirective, EnumLabelPipe],
  templateUrl: './survey-detail.component.html',
})
export class SurveyDetailComponent implements OnInit {
  private readonly surveys = inject(SurveyService);

  readonly id = input.required<string>();
  readonly survey = signal<SurveyResponse | null>(null);

  readonly orderedQuestions = computed<QuestionResponse[]>(() => {
    const survey = this.survey();
    if (!survey?.questions) return [];
    return [...survey.questions].sort((a, b) => a.sequenceOrder - b.sequenceOrder);
  });

  ngOnInit(): void {
    this.surveys.getById(Number(this.id())).subscribe((survey) => this.survey.set(survey));
  }
}
