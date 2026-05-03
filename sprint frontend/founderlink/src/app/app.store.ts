import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export type UserRole = 'ROLE_FOUNDER' | 'ROLE_INVESTOR' | 'ROLE_COFOUNDER' | 'ROLE_ADMIN';
export type StartupStage = 'IDEA' | 'MVP' | 'EARLY_TRACTION' | 'SCALING';
export type ListingStatus = 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED';
export type InvestmentStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';
export type ServiceKey =
  | 'profiles'
  | 'startups'
  | 'discover'
  | 'investments'
  | 'team'
  | 'messages'
  | 'notifications'
  | 'admin';

export interface UserProfile {
  name: string;
  email: string;
  skills: string[];
  experience: string;
  bio: string;
  portfolioLinks: string[];
  location: string;
}

export interface User {
  id: string;
  role: UserRole;
  password: string;
  profile: UserProfile;
}

interface BackendUserProfile {
  id: number;
  name: string;
  email: string;
  skills?: string;
  experience?: string;
  bio: string;
  portfolioLinks?: string;
  location?: string;
}

interface BackendStartup {
  id: number;
  title: string;
  description: string;
  domain: string;
  status: string;
  userId: number;
  problemStatement?: string;
  solution?: string;
  fundingGoal?: number;
  stage?: string;
  location?: string;
  pitch?: string;
  teamRoles?: string;
}

interface BackendInvestment {
  id: number;
  startupId: number;
  investorId: number;
  amount: number;
  status: string;
}

interface BackendTeam {
  id: number;
  startupId: number;
  userId: number;
  role: string;
  status: string;
}

interface BackendConversation {
  id: number;
  user1Id: number;
  user2Id: number;
}

interface BackendMessage {
  id: number;
  conversationId: number;
  senderId: number;
  content: string;
  timestamp: string;
}

type BackendRoleMap = Record<string, string>;

export interface Startup {
  id: string;
  founderId: string;
  startupName: string;
  description: string;
  industry: string;
  problemStatement: string;
  solution: string;
  fundingGoal: number;
  stage: StartupStage;
  location: string;
  pitch: string;
  listingStatus: ListingStatus;
  progress: number;
  followers: string[];
  teamRoles: string[];
  createdAt: string;
}

export interface Investment {
  id: string;
  startupId: string;
  investorId: string;
  amount: number;
  status: InvestmentStatus;
  createdAt: string;
}

export interface TeamInvite {
  id: string;
  startupId: string;
  invitedUserId: string;
  role: string;
  status: 'PENDING' | 'ACCEPTED';
  createdAt: string;
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  receiverId: string;
  content: string;
  createdAt: string;
}

export interface NotificationItem {
  id: string;
  userId: string;
  type: string;
  text: string;
  createdAt: string;
  read: boolean;
}

interface AppState {
  users: User[];
  startups: Startup[];
  investments: Investment[];
  invites: TeamInvite[];
  messages: Message[];
  notifications: NotificationItem[];
  sessionUserId: string;
  sessionToken: string;
}

export interface ConversationSummary {
  id: string;
  partnerId: string;
  partnerName: string;
  partnerRole: UserRole;
  lastMessage: string;
  lastTimestamp: string;
}

function resolveApiBase(): string {
  if (typeof window === 'undefined') {
    return 'http://localhost:8080';
  }

  return `${window.location.protocol}//${window.location.hostname}:8080`;
}

const API_BASE = resolveApiBase();
const STORAGE_KEY = 'founderlink-session-v2';

@Injectable({ providedIn: 'root' })
export class AppStore {
  private readonly http = inject(HttpClient);

  readonly roles: UserRole[] = ['ROLE_FOUNDER', 'ROLE_INVESTOR', 'ROLE_COFOUNDER', 'ROLE_ADMIN'];
  readonly startupStages: StartupStage[] = ['IDEA', 'MVP', 'EARLY_TRACTION', 'SCALING'];
  readonly investmentStatuses: InvestmentStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'COMPLETED'];
  readonly teamRoleOptions = ['CTO', 'CPO', 'MARKETING_HEAD', 'ENGINEERING_LEAD'];
  readonly adminQuickFilters = ['ALL', 'PENDING_REVIEW', 'APPROVED', 'REJECTED'] as const;
  readonly registerSteps = ['Choose role', 'Personal info', 'Profile details'] as const;

  readonly onboardingRoles = [
    {
      role: 'ROLE_FOUNDER' as UserRole,
      title: 'Startup Founder',
      icon: 'FL',
      description: 'Publish your startup, build a team, and attract investors.',
      action: 'Continue as Startup Founder',
      disabled: false,
    },
    {
      role: 'ROLE_INVESTOR' as UserRole,
      title: 'Investor',
      icon: 'IV',
      description: 'Discover vetted startups and deploy capital with confidence.',
      action: 'Continue as Investor',
      disabled: false,
    },
    {
      role: 'ROLE_COFOUNDER' as UserRole,
      title: 'Co-Founder',
      icon: 'CF',
      description: 'Browse startups, join teams, and build something great.',
      action: 'Continue as Co-Founder',
      disabled: false,
    },
    {
      role: 'ROLE_ADMIN' as UserRole,
      title: 'Admin',
      icon: 'AD',
      description: 'Platform administrators only. Invite required.',
      action: 'Admin access',
      disabled: true,
    },
  ];

  readonly serviceMeta: Record<ServiceKey, { title: string; subtitle: string; roles: UserRole[] }> = {
    profiles: {
      title: 'Profile Service',
      subtitle: 'Create, update, and read user profiles through /users.',
      roles: this.roles,
    },
    startups: {
      title: 'Startup Service',
      subtitle: 'Create, update, review, and browse startup listings through /startups.',
      roles: ['ROLE_FOUNDER', 'ROLE_ADMIN'],
    },
    discover: {
      title: 'Discovery Service',
      subtitle: 'Browse accepted startups from the startup service.',
      roles: ['ROLE_INVESTOR', 'ROLE_COFOUNDER', 'ROLE_ADMIN'],
    },
    investments: {
      title: 'Investment Service',
      subtitle: 'Create and view investments through /investments.',
      roles: ['ROLE_FOUNDER', 'ROLE_INVESTOR', 'ROLE_ADMIN'],
    },
    team: {
      title: 'Team Service',
      subtitle: 'Invite and join startup teams through /teams.',
      roles: ['ROLE_FOUNDER', 'ROLE_COFOUNDER', 'ROLE_ADMIN'],
    },
    messages: {
      title: 'Messaging Service',
      subtitle: 'Create conversations and send messages through /messages.',
      roles: ['ROLE_FOUNDER', 'ROLE_INVESTOR', 'ROLE_COFOUNDER', 'ROLE_ADMIN'],
    },
    notifications: {
      title: 'Notification Service',
      subtitle: 'Check notification-service status and local event activity.',
      roles: this.roles,
    },
    admin: {
      title: 'Admin Service',
      subtitle: 'Review startup status using the startup update endpoint.',
      roles: ['ROLE_ADMIN'],
    },
  };

  readonly state = signal<AppState>(this.loadState());
  readonly authMode = signal<'login' | 'register'>('login');
  readonly registerStep = signal<1 | 2 | 3>(1);
  readonly selectedStartupId = signal('');
  readonly showSelectedStartupPreview = signal(false);
  readonly selectedConversationId = signal('');
  readonly selectedMessagePartnerId = signal('');
  readonly adminFilter = signal<(typeof this.adminQuickFilters)[number]>('ALL');
  readonly rememberMe = signal(true);
  readonly busy = signal(false);
  readonly backendMessage = signal('');
  readonly pendingVerificationEmail = signal('');
  readonly verificationCode = signal('');
  readonly forgotPasswordStep = signal<0 | 1 | 2>(0);
  readonly forgotPasswordEmail = signal('');
  readonly forgotPasswordOtp = signal('');
  readonly forgotPasswordNewPassword = signal('');
  readonly forgotPasswordConfirmPassword = signal('');
  readonly showLoginPassword = signal(false);

  readonly loginForm = signal({
    email: '',
    password: '',
    role: 'ROLE_FOUNDER' as UserRole,
  });

  readonly registerForm = signal({
    name: '',
    email: '',
    password: '',
    role: 'ROLE_FOUNDER' as UserRole,
    skills: '',
    experience: '',
    bio: '',
    portfolioLinks: '',
    location: 'Bengaluru, India',
  });

  readonly profileDraft = signal<UserProfile>(this.emptyProfileDraft());
  readonly startupDraft = signal({
    id: '',
    startupName: '',
    description: '',
    industry: 'FinTech',
    problemStatement: '',
    solution: '',
    fundingGoal: 2500000,
    stage: 'MVP' as StartupStage,
    location: 'Bengaluru, India',
    pitch: '',
    teamRoles: 'CTO, MARKETING_HEAD',
  });
  readonly searchFilters = signal({
    industry: 'ALL',
    stage: 'ALL',
    fundingGoal: 10000000,
    location: '',
  });
  readonly investmentDraft = signal({
    startupId: '',
    amount: 500000,
  });
  readonly inviteDraft = signal({
    startupId: '',
    invitedUserId: '',
    role: 'CTO',
  });
  readonly messageDraft = signal('');
  private selectedStartupPreviewTimer: ReturnType<typeof setTimeout> | null = null;
  private messageRefreshTimer: ReturnType<typeof setInterval> | null = null;

  readonly isLoggedIn = computed(() => !!this.state().sessionUserId && !!this.state().sessionToken);
  readonly currentUser = computed(
    () => this.state().users.find((user) => user.id === this.state().sessionUserId) ?? null,
  );
  readonly currentRole = computed(() => this.currentUser()?.role ?? null);
  readonly approvedStartups = computed(() =>
    this.state().startups.filter((startup) => startup.listingStatus === 'APPROVED'),
  );
  readonly visibleStartups = computed(() => {
    const filters = this.searchFilters();
    return this.approvedStartups().filter((startup) => {
      const matchesIndustry = filters.industry === 'ALL' || startup.industry === filters.industry;
      const matchesStage = filters.stage === 'ALL' || startup.stage === filters.stage;
      const matchesGoal = startup.fundingGoal <= Number(filters.fundingGoal);
      const matchesLocation =
        !filters.location ||
        startup.location.toLowerCase().includes(filters.location.trim().toLowerCase());
      return matchesIndustry && matchesStage && matchesGoal && matchesLocation;
    });
  });
  readonly myStartups = computed(() => {
    const user = this.currentUser();
    return user ? this.state().startups.filter((startup) => startup.founderId === user.id) : [];
  });
  readonly managedStartups = computed(() =>
    this.currentRole() === 'ROLE_ADMIN' ? this.state().startups : this.myStartups(),
  );
  readonly myNotifications = computed(() => {
    const user = this.currentUser();
    return user
      ? this.state()
          .notifications.filter((item) => item.userId === user.id)
          .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
      : [];
  });
  readonly unreadNotificationCount = computed(
    () => this.myNotifications().filter((item) => !item.read).length,
  );
  readonly authStats = computed(() => {
    const investments = this.state().investments.filter((investment) => investment.status !== 'REJECTED');
    return {
      founders: this.state().users.filter((user) => user.role === 'ROLE_FOUNDER').length,
      investors: this.state().users.filter((user) => user.role === 'ROLE_INVESTOR').length,
      totalRaised: investments.reduce((sum, investment) => sum + Number(investment.amount), 0),
    };
  });
  readonly founderInvestmentQueue = computed(() => {
    const startupIds = new Set(this.myStartups().map((startup) => startup.id));
    return this.state().investments.filter((investment) => startupIds.has(investment.startupId));
  });
  readonly investorPortfolio = computed(() => {
    const user = this.currentUser();
    return user ? this.state().investments.filter((investment) => investment.investorId === user.id) : [];
  });
  readonly incomingInvites = computed(() => {
    const user = this.currentUser();
    return user ? this.state().invites.filter((invite) => invite.invitedUserId === user.id) : [];
  });
  readonly startupOpportunities = computed(() => this.approvedStartups());
  readonly adminStartups = computed(() => {
    const filter = this.adminFilter();
    return filter === 'ALL'
      ? this.state().startups
      : this.state().startups.filter((startup) => startup.listingStatus === filter);
  });
  readonly dashboardStats = computed(() => {
    const startups = this.state().startups;
    const investments = this.state().investments;
    const approvedCount = startups.filter((startup) => startup.listingStatus === 'APPROVED').length;
    const pendingCount = startups.filter((startup) => startup.listingStatus === 'PENDING_REVIEW').length;
    const activeInvestments = investments.filter((investment) => investment.status !== 'REJECTED');
    const raised = activeInvestments.reduce((sum, investment) => sum + Number(investment.amount), 0);
    const fundingGoal = startups.reduce((sum, startup) => sum + Number(startup.fundingGoal), 0);
    return {
      startups: startups.length,
      approvedCount,
      pendingCount,
      totalUsers: this.state().users.length,
      activeConversations: new Set(this.state().messages.map((message) => message.conversationId)).size,
      totalRaised: raised,
      completionRate: fundingGoal ? Math.min(raised / fundingGoal, 1) : 0,
    };
  });
  readonly conversations = computed<ConversationSummary[]>(() => {
    const user = this.currentUser();
    if (!user) {
      return [];
    }
    const map = new Map<string, ConversationSummary>();
    const messages = this.state()
      .messages.filter((message) => message.senderId === user.id || message.receiverId === user.id)
      .sort((a, b) => a.createdAt.localeCompare(b.createdAt));

    for (const message of messages) {
      const partnerId = message.senderId === user.id ? message.receiverId : message.senderId;
      const partner = this.getUserById(partnerId);
      if (!partner) {
        continue;
      }
      map.set(message.conversationId, {
        id: message.conversationId,
        partnerId,
        partnerName: partner.profile.name,
        partnerRole: partner.role,
        lastMessage: message.content,
        lastTimestamp: message.createdAt,
      });
    }
    return [...map.values()].sort((a, b) => b.lastTimestamp.localeCompare(a.lastTimestamp));
  });
  readonly activeConversation = computed(
    () => this.conversations().find((conversation) => conversation.id === this.selectedConversationId()) ?? null,
  );
  readonly activeMessagePartner = computed(() => {
    const user = this.currentUser();
    const activeConversation = this.activeConversation();
    if (activeConversation) {
      const partner = this.getUserById(activeConversation.partnerId) ?? null;
      return user && partner && this.canMessageRole(user.role, partner.role) ? partner : null;
    }
    const partner = this.selectedMessagePartnerId() ? this.getUserById(this.selectedMessagePartnerId()) ?? null : null;
    return user && partner && partner.id !== user.id && this.canMessageRole(user.role, partner.role) ? partner : null;
  });
  readonly conversationMessages = computed(() =>
    this.selectedConversationId()
      ? this.state()
          .messages.filter((message) => message.conversationId === this.selectedConversationId())
          .sort((a, b) => a.createdAt.localeCompare(b.createdAt))
      : [],
  );
  readonly accessibleServices = computed(() => {
    const role = this.currentRole();
    if (!role) {
      return [];
    }
    if (role === 'ROLE_ADMIN') {
      return ['discover', 'messages', 'admin'] as ServiceKey[];
    }
    return (Object.keys(this.serviceMeta) as ServiceKey[]).filter((key) =>
      this.serviceMeta[key].roles.includes(role),
    );
  });

  constructor() {
    effect(() => {
      if (typeof window !== 'undefined') {
        window.localStorage.setItem(STORAGE_KEY, JSON.stringify({
          sessionUserId: this.state().sessionUserId,
          sessionToken: this.state().sessionToken,
          users: this.state().users,
          startups: this.state().startups,
          investments: this.state().investments,
          invites: this.state().invites,
          messages: this.state().messages,
          notifications: this.state().notifications,
        }));
      }
    });

    effect(() => {
      const user = this.currentUser();
      this.profileDraft.set(user ? { ...user.profile } : this.emptyProfileDraft());
    });

    if (this.state().sessionToken) {
      queueMicrotask(() => {
        this.refreshBackendData().catch((error) => this.backendMessage.set(this.describeError(error)));
      });
    }
    if (typeof window !== 'undefined') {
      this.messageRefreshTimer = setInterval(() => {
        if (this.isLoggedIn()) {
          this.refreshMessagesForCurrentUser().catch(() => undefined);
        }
      }, 5000);
    }
  }

  setAuthMode(mode: 'login' | 'register'): void {
    this.authMode.set(mode);
    this.registerStep.set(1);
    this.backendMessage.set('');
  }

  clearBackendMessage(): void {
    this.backendMessage.set('');
  }

  openForgotPassword(): void {
    this.forgotPasswordEmail.set(this.loginForm().email.trim());
    this.forgotPasswordOtp.set('');
    this.forgotPasswordNewPassword.set('');
    this.forgotPasswordConfirmPassword.set('');
    this.forgotPasswordStep.set(1);
    this.backendMessage.set('');
  }

  closeForgotPassword(): void {
    this.forgotPasswordStep.set(0);
    this.forgotPasswordOtp.set('');
    this.forgotPasswordNewPassword.set('');
    this.forgotPasswordConfirmPassword.set('');
    this.backendMessage.set('');
  }

  setRegisterStep(step: 1 | 2 | 3): void {
    this.registerStep.set(step);
  }

  selectRegisterRole(role: UserRole): void {
    if (role !== 'ROLE_ADMIN') {
      this.updateRegisterField('role', role);
    }
  }

  nextRegisterStep(): void {
    if (this.registerStep() < 3) {
      this.registerStep.set((this.registerStep() + 1) as 1 | 2 | 3);
    }
  }

  previousRegisterStep(): void {
    if (this.registerStep() > 1) {
      this.registerStep.set((this.registerStep() - 1) as 1 | 2 | 3);
    }
  }

  setAdminFilter(filter: (typeof this.adminQuickFilters)[number]): void {
    this.adminFilter.set(filter);
  }

  updateLoginField<K extends keyof ReturnType<typeof this.loginForm>>(
    field: K,
    value: ReturnType<typeof this.loginForm>[K],
  ): void {
    this.loginForm.update((draft) => ({ ...draft, [field]: value }));
  }

  updateRegisterField<K extends keyof ReturnType<typeof this.registerForm>>(
    field: K,
    value: ReturnType<typeof this.registerForm>[K],
  ): void {
    this.registerForm.update((draft) => ({ ...draft, [field]: value }));
  }

  updateProfileField(field: keyof UserProfile, value: string): void {
    if (field !== 'skills' && field !== 'portfolioLinks') {
      this.profileDraft.update((draft) => ({ ...draft, [field]: value }));
    }
  }

  updateProfileList(field: 'skills' | 'portfolioLinks', value: string): void {
    this.profileDraft.update((draft) => ({ ...draft, [field]: this.toList(value) }));
  }

  updateStartupDraft<K extends keyof ReturnType<typeof this.startupDraft>>(
    field: K,
    value: ReturnType<typeof this.startupDraft>[K],
  ): void {
    this.startupDraft.update((draft) => ({ ...draft, [field]: value }));
  }

  updateSearchFilters<K extends keyof ReturnType<typeof this.searchFilters>>(
    field: K,
    value: ReturnType<typeof this.searchFilters>[K],
  ): void {
    this.searchFilters.update((draft) => ({ ...draft, [field]: value }));
  }

  updateInvestmentDraft<K extends keyof ReturnType<typeof this.investmentDraft>>(
    field: K,
    value: ReturnType<typeof this.investmentDraft>[K],
  ): void {
    this.investmentDraft.update((draft) => ({ ...draft, [field]: value }));
  }

  updateInviteDraft<K extends keyof ReturnType<typeof this.inviteDraft>>(
    field: K,
    value: ReturnType<typeof this.inviteDraft>[K],
  ): void {
    this.inviteDraft.update((draft) => ({ ...draft, [field]: value }));
  }

  async login(): Promise<boolean> {
    const form = this.loginForm();
    return this.runBackend(async () => {
      const token = await firstValueFrom(
        this.http.post(`${API_BASE}/auth/login`, { email: form.email, password: form.password }, {
          responseType: 'text',
        }),
      );
      const claims = this.decodeJwt(token);
      const userId = String(claims.sub);
      const role = this.normalizeRole((claims.roles?.[0] as string) ?? form.role);
      const existingUser = this.state().users.find((user) => user.id === userId);
      const fallbackUser: User = existingUser ?? {
        id: userId,
        role,
        password: form.password,
        profile: {
          name: form.email.split('@')[0],
          email: form.email,
          skills: [],
          experience: '',
          bio: '',
          portfolioLinks: [],
          location: 'India',
        },
      };
      this.state.update((state) => ({
        ...state,
        users: this.upsertUser(state.users, { ...fallbackUser, role, password: form.password }),
        sessionUserId: userId,
        sessionToken: token,
      }));
      this.clearConversationSelection();
      this.pendingVerificationEmail.set('');
      this.verificationCode.set('');
      queueMicrotask(() => {
        this.refreshBackendData().catch((error) => this.backendMessage.set(this.describeError(error)));
      });
      return true;
    }, false);
  }

  logout(): void {
    this.state.update((state) => ({ ...state, sessionUserId: '', sessionToken: '' }));
    this.clearConversationSelection();
    this.backendMessage.set('');
  }

  async register(): Promise<boolean> {
    const form = this.registerForm();
    if (!form.email || !form.password || !form.role) {
      return false;
    }
    return this.runBackend(async () => {
      const message = await firstValueFrom(
        this.http.post(
          `${API_BASE}/auth/register`,
          {
            ...this.authPayload(form.email, form.password, form.role),
            name: form.name,
            bio: form.bio,
          },
          { responseType: 'text' },
        ),
      );
      this.pendingVerificationEmail.set(form.email);
      this.verificationCode.set('');
      this.backendMessage.set(`${message} Enter the OTP below to finish verification.`);
      this.loginForm.set({ email: form.email, password: form.password, role: form.role });
      return true;
    }, false);
  }

  async verifyOtp(): Promise<boolean> {
    const email = this.pendingVerificationEmail().trim();
    const otp = this.verificationCode().trim();
    if (!email || !otp) {
      return false;
    }

    return this.runBackend(async () => {
      const message = await firstValueFrom(
        this.http.post(
          `${API_BASE}/auth/verify-otp`,
          { email, otp },
          { responseType: 'text' },
        ),
      );
      this.pendingVerificationEmail.set('');
      this.verificationCode.set('');
      this.setAuthMode('login');
      this.backendMessage.set(`${message} You can sign in now.`);
      return true;
    }, false);
  }

  async resendOtp(): Promise<boolean> {
    const email = this.pendingVerificationEmail().trim() || this.registerForm().email.trim();
    if (!email) {
      return false;
    }

    return this.runBackend(async () => {
      const message = await firstValueFrom(
        this.http.post(
          `${API_BASE}/auth/resend-otp`,
          { email },
          { responseType: 'text' },
        ),
      );
      this.pendingVerificationEmail.set(email);
      this.backendMessage.set(message);
      return true;
    }, false);
  }

  async requestPasswordResetOtp(): Promise<boolean> {
    const email = this.forgotPasswordEmail().trim();
    if (!email) {
      this.backendMessage.set('Enter your account email address first.');
      return false;
    }

    return this.runBackend(async () => {
      const message = await firstValueFrom(
        this.http.post(`${API_BASE}/auth/forgot-password`, { email }, { responseType: 'text' }),
      );
      this.forgotPasswordEmail.set(email);
      this.forgotPasswordStep.set(1);
      this.backendMessage.set(String(message));
      return true;
    }, false);
  }

  async verifyPasswordResetOtp(): Promise<boolean> {
    const email = this.forgotPasswordEmail().trim();
    const otp = this.forgotPasswordOtp().trim();
    if (!email || !otp) {
      this.backendMessage.set('Enter the OTP sent to your email.');
      return false;
    }

    return this.runBackend(async () => {
      const message = await firstValueFrom(
        this.http.post(`${API_BASE}/auth/forgot-password/verify`, { email, otp }, { responseType: 'text' }),
      );
      this.forgotPasswordStep.set(2);
      this.backendMessage.set(String(message));
      return true;
    }, false);
  }

  async resetPassword(): Promise<boolean> {
    const email = this.forgotPasswordEmail().trim();
    const otp = this.forgotPasswordOtp().trim();
    const password = this.forgotPasswordNewPassword();
    const confirmPassword = this.forgotPasswordConfirmPassword();
    if (!password || password.length < 6) {
      this.backendMessage.set('Password must be at least 6 characters.');
      return false;
    }
    if (password !== confirmPassword) {
      this.backendMessage.set('Passwords do not match.');
      return false;
    }

    return this.runBackend(async () => {
      const message = await firstValueFrom(
        this.http.post(
          `${API_BASE}/auth/reset-password`,
          { email, otp, password, confirmPassword },
          { responseType: 'text' },
        ),
      );
      this.loginForm.update((form) => ({ ...form, email, password }));
      this.forgotPasswordStep.set(0);
      this.forgotPasswordOtp.set('');
      this.forgotPasswordNewPassword.set('');
      this.forgotPasswordConfirmPassword.set('');
      this.backendMessage.set(String(message));
      return true;
    }, false);
  }

  async saveProfile(): Promise<void> {
    const user = this.currentUser();
    if (!user) {
      return;
    }
    const draft = this.profileDraft();
    await this.runBackend(async () => {
      const updated = await firstValueFrom(
        this.http.put<BackendUserProfile>(
          `${API_BASE}/users/${user.id}`,
          {
            name: draft.name,
            email: draft.email,
            skills: draft.skills.join(', '),
            experience: draft.experience,
            location: draft.location,
            bio: draft.bio,
            portfolioLinks: draft.portfolioLinks.join(', '),
          },
          { headers: this.headers() },
        ),
      );
      const mergedUser: User = {
        ...user,
        profile: {
          ...draft,
          name: updated.name,
          email: updated.email,
          skills: this.toList(updated.skills ?? draft.skills.join(', ')),
          experience: updated.experience ?? draft.experience,
          location: updated.location ?? draft.location,
          bio: updated.bio ?? draft.bio,
          portfolioLinks: this.toList(updated.portfolioLinks ?? draft.portfolioLinks.join(', ')),
        },
      };
      this.state.update((state) => ({ ...state, users: this.upsertUser(state.users, mergedUser) }));
      this.backendMessage.set('Profile saved successfully.');
      this.pushNotification('PROFILE_UPDATED', 'Profile saved to user-service.');
    });
  }

  async saveStartup(): Promise<void> {
    const user = this.currentUser();
    if (!user) {
      return;
    }
    const draft = this.startupDraft();
    const payload = this.toBackendStartupPayload(draft, user.id);
    await this.runBackend(async () => {
      const saved = draft.id
        ? await firstValueFrom(this.http.put<BackendStartup>(`${API_BASE}/startups/${draft.id}`, payload, { headers: this.headers() }))
        : await firstValueFrom(this.http.post<BackendStartup>(`${API_BASE}/startups`, payload, { headers: this.headers() }));
      this.state.update((state) => ({ ...state, startups: this.upsertStartup(state.startups, this.fromBackendStartup(saved)) }));
      this.startupDraft.update((current) => ({ ...current, id: String(saved.id) }));
      this.selectedStartupId.set(String(saved.id));
      this.backendMessage.set(`${saved.title} saved. It will appear in Discovery after admin approval.`);
      this.pushNotification('STARTUP_SAVED', `${saved.title} saved through startup-service.`);
    });
  }

  editStartup(startupId: string): void {
    const startup = this.getStartupById(startupId);
    if (!startup) {
      return;
    }
    this.startupDraft.set({
      id: startup.id,
      startupName: startup.startupName,
      description: startup.description,
      industry: startup.industry,
      problemStatement: startup.problemStatement,
      solution: startup.solution,
      fundingGoal: startup.fundingGoal,
      stage: startup.stage,
      location: startup.location,
      pitch: startup.pitch,
      teamRoles: startup.teamRoles.join(', '),
    });
  }

  async deleteStartup(startupId: string): Promise<void> {
    await this.runBackend(async () => {
      await firstValueFrom(this.http.delete(`${API_BASE}/startups/${startupId}`, { headers: this.headers() }));
      this.state.update((state) => ({
        ...state,
        startups: state.startups.filter((startup) => startup.id !== startupId),
      }));
      this.resetStartupDraft();
    });
  }

  followStartup(startupId: string): void {
    const user = this.currentUser();
    if (!user) {
      return;
    }
    this.state.update((state) => ({
      ...state,
      startups: state.startups.map((startup) =>
        startup.id === startupId
          ? {
              ...startup,
              followers: startup.followers.includes(user.id)
                ? startup.followers.filter((id) => id !== user.id)
                : [...startup.followers, user.id],
            }
          : startup,
      ),
    }));
  }

  async createInvestment(): Promise<void> {
    const user = this.currentUser();
    const draft = this.investmentDraft();
    if (!user || !draft.startupId) {
      return;
    }
    const startup = this.getStartupById(draft.startupId);
    await this.runBackend(async () => {
      const saved = await firstValueFrom(
        this.http.post<BackendInvestment>(
          `${API_BASE}/investments`,
          { startupId: Number(draft.startupId), investorId: Number(user.id), amount: Number(draft.amount) },
          { headers: this.headers() },
        ),
      );
      this.state.update((state) => ({ ...state, investments: this.upsertInvestment(state.investments, this.fromBackendInvestment(saved)) }));
      this.backendMessage.set(
        `Investment request sent${startup ? ` for ${startup.startupName}` : ''}. The founder will receive an email and can approve it after login.`,
      );
      this.pushNotification('INVESTMENT_CREATED', 'Investment request saved through investment-service.');
      if (startup) {
        this.pushNotificationForUser(
          startup.founderId,
          'INVESTMENT_REQUESTED',
          `${user.profile.name} requested ${this.formatCurrency(Number(saved.amount))} for ${startup.startupName}.`,
        );
      }
    });
  }

  async updateInvestmentStatus(investmentId: string, status: InvestmentStatus): Promise<void> {
    await this.runBackend(async () => {
      const saved = await firstValueFrom(
        this.http.put<BackendInvestment>(
          `${API_BASE}/investments/${investmentId}/status`,
          { status },
          { headers: this.headers() },
        ),
      );
      this.state.update((state) => ({
        ...state,
        investments: state.investments.map((investment) =>
          investment.id === investmentId ? this.fromBackendInvestment(saved) : investment,
        ),
      }));
      this.pushNotification('INVESTMENT_STATUS', `Investment updated to ${saved.status}.`);
    });
  }

  async sendInvite(): Promise<void> {
    const draft = this.inviteDraft();
    if (!draft.startupId || !draft.invitedUserId) {
      return;
    }
    await this.runBackend(async () => {
      const saved = await firstValueFrom(
        this.http.post<BackendTeam>(
          `${API_BASE}/teams/invite`,
          { startupId: Number(draft.startupId), userId: Number(draft.invitedUserId), role: draft.role },
          { headers: this.headers() },
        ),
      );
      this.state.update((state) => ({ ...state, invites: this.upsertInvite(state.invites, this.fromBackendTeam(saved)) }));
      this.pushNotification('TEAM_INVITE_SENT', 'Team invite saved through team-service.');
    });
  }

  async acceptInvite(inviteId: string): Promise<void> {
    const invite = this.state().invites.find((item) => item.id === inviteId);
    if (!invite) {
      return;
    }
    await this.runBackend(async () => {
      const saved = await firstValueFrom(
        this.http.post<BackendTeam>(
          `${API_BASE}/teams/join`,
          { startupId: Number(invite.startupId), userId: Number(invite.invitedUserId), role: invite.role },
          { headers: this.headers() },
        ),
      );
      this.state.update((state) => ({ ...state, invites: this.upsertInvite(state.invites, this.fromBackendTeam(saved)) }));
      this.pushNotification('TEAM_JOINED', 'Team membership activated through team-service.');
    });
  }

  async openConversation(partnerId: string): Promise<void> {
    const user = this.currentUser();
    const partner = this.getUserById(partnerId);
    if (!user || !partner || !this.canMessageRole(user.role, partner.role)) {
      this.clearConversationSelection();
      return;
    }
    const existing = this.conversations().find((conversation) => conversation.partnerId === partnerId);
    if (existing) {
      this.selectedConversationId.set(existing.id);
      this.selectedMessagePartnerId.set(partnerId);
      return;
    }
    await this.runBackend(async () => {
      const conversation = await firstValueFrom(
        this.http.post<BackendConversation>(
          `${API_BASE}/messages/conversation`,
          { user1Id: Number(user.id), user2Id: Number(partnerId) },
          { headers: this.headers() },
        ),
      );
      const id = String(conversation.id);
      this.selectedConversationId.set(id);
      this.selectedMessagePartnerId.set(partnerId);
    });
  }

  private clearConversationSelection(): void {
    this.selectedConversationId.set('');
    this.selectedMessagePartnerId.set('');
    this.messageDraft.set('');
  }

  async sendMessage(): Promise<void> {
    const user = this.currentUser();
    const conversationId = this.selectedConversationId();
    const partner = this.activeMessagePartner();
    const content = this.messageDraft().trim();
    if (!user || !conversationId || !partner || !content) {
      return;
    }
    await this.runBackend(async () => {
      const saved = await firstValueFrom(
        this.http.post<BackendMessage>(
          `${API_BASE}/messages`,
          { conversationId: Number(conversationId), senderId: Number(user.id), content },
          { headers: this.headers() },
        ),
      );
      this.state.update((state) => ({
        ...state,
        messages: [...state.messages, this.fromBackendMessage(saved, partner.id)],
      }));
      this.pushNotificationForUser(
        partner.id,
        'MESSAGE_RECEIVED',
        `${user.profile.name} sent you a new message.`,
      );
      this.messageDraft.set('');
      this.refreshMessagesForCurrentUser().catch(() => undefined);
    });
  }

  markNotificationsRead(): void {
    const user = this.currentUser();
    if (!user) {
      return;
    }
    this.state.update((state) => ({
      ...state,
      notifications: state.notifications.map((item) =>
        item.userId === user.id ? { ...item, read: true } : item,
      ),
    }));
  }

  async reviewStartup(startupId: string, decision: ListingStatus): Promise<void> {
    const startup = this.getStartupById(startupId);
    if (!startup) {
      return;
    }
    const status = decision === 'APPROVED' ? 'ACCEPTED' : decision === 'REJECTED' ? 'REJECTED' : 'PENDING';
    await this.runBackend(async () => {
      const saved = await firstValueFrom(
        this.http.put<BackendStartup>(
          `${API_BASE}/startups/${startupId}`,
          { title: startup.startupName, description: startup.description, domain: startup.industry, status, userId: Number(startup.founderId) },
          { headers: this.headers() },
        ),
      );
      this.state.update((state) => ({ ...state, startups: this.upsertStartup(state.startups, this.fromBackendStartup(saved)) }));
      this.pushNotification('STARTUP_REVIEW', `${saved.title} is now ${saved.status}.`);
    });
  }

  useSelectedStartupForInvestment(startupId: string): void {
    this.selectedStartupId.set(startupId);
    this.investmentDraft.update((draft) => ({ ...draft, startupId }));
  }

  selectStartup(startupId: string): void {
    const startup = this.getStartupById(startupId);
    this.selectedStartupId.set(startupId);
    this.showSelectedStartupPreview.set(true);
    if (this.selectedStartupPreviewTimer) {
      clearTimeout(this.selectedStartupPreviewTimer);
    }
    this.selectedStartupPreviewTimer = setTimeout(() => {
      if (this.selectedStartupId() === startupId) {
        this.showSelectedStartupPreview.set(false);
        this.selectedStartupId.set('');
      }
    }, 4000);
    if (startup) {
      this.backendMessage.set(`${startup.startupName} is selected. Details are shown on the right.`);
      setTimeout(() => {
        if (this.backendMessage().startsWith(`${startup.startupName} is selected.`)) {
          this.backendMessage.set('');
        }
      }, 4000);
    }
  }

  resetStartupDraft(): void {
    this.startupDraft.set({
      id: '',
      startupName: '',
      description: '',
      industry: 'FinTech',
      problemStatement: '',
      solution: '',
      fundingGoal: 2500000,
      stage: 'MVP',
      location: 'Bengaluru, India',
      pitch: '',
      teamRoles: 'CTO, MARKETING_HEAD',
    });
  }

  isFollowing(startupId: string): boolean {
    const user = this.currentUser();
    const startup = this.getStartupById(startupId);
    return !!user && !!startup && startup.followers.includes(user.id);
  }

  getSelectedStartup(service?: ServiceKey): Startup | null {
    if (this.selectedStartupId()) {
      const selected = this.getStartupById(this.selectedStartupId());
      if (selected) {
        return selected;
      }
    }
    if (service === 'startups' || service === 'team') {
      return this.myStartups()[0] ?? null;
    }
    if (service === 'admin') {
      return this.adminStartups()[0] ?? null;
    }
    return this.getStartupById(this.selectedStartupId()) ?? this.visibleStartups()[0] ?? null;
  }

  getTeamMemberNames(startupId: string): string {
    const invitees = this.state().invites.filter(
      (invite) => invite.startupId === startupId && invite.status === 'ACCEPTED',
    );
    return invitees.map((invite) => this.getUserName(invite.invitedUserId)).join(', ') || 'No accepted invites yet';
  }

  getPendingInviteCount(startupId: string): number {
    return this.state().invites.filter(
      (invite) => invite.startupId === startupId && invite.status === 'PENDING',
    ).length;
  }

  getStartupName(startupId: string): string {
    return this.getStartupById(startupId)?.startupName ?? 'Unknown Startup';
  }

  getUserName(userId: string): string {
    return this.getUserById(userId)?.profile.name ?? `User ${userId}`;
  }

  getPotentialInvitees(): User[] {
    const currentUserId = this.currentUser()?.id;
    return this.state().users.filter((user) => user.id !== currentUserId);
  }

  getMessageContacts(): User[] {
    const currentUser = this.currentUser();
    if (!currentUser) {
      return [];
    }
    return this.state()
      .users.filter((user) => user.id !== currentUser.id && this.canMessageRole(currentUser.role, user.role))
      .sort((a, b) => a.profile.name.localeCompare(b.profile.name));
  }

  getIndustries(): string[] {
    return ['ALL', ...new Set(this.state().startups.map((startup) => startup.industry).filter(Boolean))];
  }

  getRoleBadgeClass(role: UserRole): string {
    return role.toLowerCase().replace('role_', 'role-');
  }

  canContinueRegister(): boolean {
    const form = this.registerForm();
    if (this.registerStep() === 1) {
      return form.role !== 'ROLE_ADMIN';
    }
    if (this.registerStep() === 2) {
      return !!form.name && !!form.email && !!form.password && !!form.location;
    }
    return !!form.experience && !!form.bio;
  }

  progressFor(startup: Startup): number {
    const raised = this.state()
      .investments.filter((investment) => investment.startupId === startup.id && investment.status !== 'REJECTED')
      .reduce((sum, investment) => sum + investment.amount, 0);
    return Math.min(raised / startup.fundingGoal, 1);
  }

  remainingGoal(startup: Startup): number {
    return Math.max(startup.fundingGoal - this.progressFor(startup) * startup.fundingGoal, 0);
  }

  prettyRole(role: UserRole): string {
    return role.replace('ROLE_', '').replace('_', '-');
  }

  messageTimeLabel(value: string): string {
    const date = new Date(value);
    const safeDate = Number.isNaN(date.getTime()) ? new Date() : date;
    const istDay = (input: Date): Date => {
      const parts = new Intl.DateTimeFormat('en-IN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        timeZone: 'Asia/Kolkata',
      }).formatToParts(input);
      const part = (type: string) => Number(parts.find((item) => item.type === type)?.value ?? 0);
      return new Date(Date.UTC(part('year'), part('month') - 1, part('day')));
    };
    const today = istDay(new Date());
    const messageDay = istDay(safeDate);
    const dayDiff = Math.round((today.getTime() - messageDay.getTime()) / 86400000);
    const time = safeDate.toLocaleTimeString('en-IN', {
      hour: 'numeric',
      minute: '2-digit',
      timeZone: 'Asia/Kolkata',
    });

    if (dayDiff === 0) {
      return `Today, ${time}`;
    }
    if (dayDiff === 1) {
      return `Yesterday, ${time}`;
    }
    return `${safeDate.toLocaleDateString('en-IN', {
      month: 'short',
      day: 'numeric',
      timeZone: 'Asia/Kolkata',
    })}, ${time}`;
  }

  private async refreshBackendData(): Promise<void> {
    const token = this.state().sessionToken;
    if (!token) {
      return;
    }
    const user = this.currentUser();
    const headers = this.authHeaders(token);
    const [startups, profiles, roleMap] = await Promise.all([
      firstValueFrom(this.http.get<BackendStartup[]>(`${API_BASE}/startups`, { headers })),
      firstValueFrom(this.http.get<BackendUserProfile[]>(`${API_BASE}/users/directory`, { headers })),
      this.loadUserRoles(),
    ]);
    const mappedStartups = startups.map((startup) => this.fromBackendStartup(startup));

    let users = profiles.map((profile) =>
      this.toUser(profile, this.normalizeRole(roleMap[String(profile.id)] ?? this.roleForEmail(profile.email)), ''),
    );
    if (user) {
      const currentProfile = profiles.find((profile) => String(profile.id) === user.id);
      users = this.upsertUser(users, {
        ...user,
        profile: {
          ...user.profile,
          name: currentProfile?.name ?? user.profile.name,
          email: currentProfile?.email ?? user.profile.email,
          skills: this.toList(currentProfile?.skills ?? user.profile.skills.join(', ')),
          experience: currentProfile?.experience ?? user.profile.experience,
          location: currentProfile?.location ?? user.profile.location,
          bio: currentProfile?.bio ?? user.profile.bio,
          portfolioLinks: this.toList(currentProfile?.portfolioLinks ?? user.profile.portfolioLinks.join(', ')),
        },
      });
    }

    const [investments, teams, messages, notificationStatus] = await Promise.all([
      this.loadInvestments(mappedStartups, user),
      this.loadTeams(mappedStartups),
      this.loadMessages(user),
      this.loadNotificationStatus(),
    ]);

    this.state.update((state) => ({
      ...state,
      users,
      startups: mappedStartups,
      investments,
      invites: teams,
      messages,
      notifications: notificationStatus ? this.upsertNotification(state.notifications, notificationStatus) : state.notifications,
    }));
  }

  private async loadUserRoles(): Promise<BackendRoleMap> {
    try {
      return await firstValueFrom(this.http.get<BackendRoleMap>(`${API_BASE}/auth/user-roles`));
    } catch {
      return {};
    }
  }

  private async loadInvestments(startups: Startup[], user: User | null): Promise<Investment[]> {
    if (!user) {
      return [];
    }
    try {
      const rows = await firstValueFrom(
        this.http.get<BackendInvestment[]>(`${API_BASE}/investments`, { headers: this.headers() }),
      );
      return rows.map((row) => this.fromBackendInvestment(row));
    } catch {
      // Fall back to role-scoped endpoints for older containers until investment-service is rebuilt.
    }

    if (user.role === 'ROLE_INVESTOR') {
      const rows = await firstValueFrom(
        this.http.get<BackendInvestment[]>(`${API_BASE}/investments/investor/${user.id}`, { headers: this.headers() }),
      );
      return rows.map((row) => this.fromBackendInvestment(row));
    }
    if (user.role === 'ROLE_FOUNDER' || user.role === 'ROLE_ADMIN') {
      const mine = user.role === 'ROLE_ADMIN' ? startups : startups.filter((startup) => startup.founderId === user.id);
      const batches = await Promise.all(
        mine.map((startup) =>
          firstValueFrom(this.http.get<BackendInvestment[]>(`${API_BASE}/investments/startup/${startup.id}`, { headers: this.headers() }))
            .catch(() => []),
        ),
      );
      return batches.flat().map((row) => this.fromBackendInvestment(row));
    }
    const batches = await Promise.all(
      startups.map((startup) =>
        firstValueFrom(this.http.get<BackendInvestment[]>(`${API_BASE}/investments/startup/${startup.id}`, { headers: this.headers() }))
          .catch(() => []),
      ),
    );
    return batches.flat().map((row) => this.fromBackendInvestment(row));
  }

  private async loadTeams(startups: Startup[]): Promise<TeamInvite[]> {
    const batches = await Promise.all(
      startups.map((startup) =>
        firstValueFrom(this.http.get<BackendTeam[]>(`${API_BASE}/teams/startup/${startup.id}`, { headers: this.headers() }))
          .catch(() => []),
      ),
    );
    return batches.flat().map((row) => this.fromBackendTeam(row));
  }

  private async loadMessages(user: User | null): Promise<Message[]> {
    if (!user) {
      return [];
    }

    try {
      const conversations = await firstValueFrom(
        this.http.get<BackendConversation[]>(`${API_BASE}/messages/user/${user.id}/conversations`, { headers: this.headers() }),
      );
      const batches = await Promise.all(
        conversations.map((conversation) =>
          firstValueFrom(
            this.http.get<BackendMessage[]>(`${API_BASE}/messages/conversation/${conversation.id}`, { headers: this.headers() }),
          )
            .then((messages) =>
              messages.map((message) =>
                this.fromBackendMessage(message, this.partnerIdForConversation(conversation, message.senderId)),
              ),
            )
            .catch(() => []),
        ),
      );
      return batches.flat();
    } catch {
      return [];
    }
  }

  private async refreshMessagesForCurrentUser(): Promise<void> {
    const user = this.currentUser();
    if (!user || !this.state().sessionToken) {
      return;
    }
    const messages = await this.loadMessages(user);
    this.state.update((state) => ({ ...state, messages }));
  }

  private async loadNotificationStatus(): Promise<NotificationItem | null> {
    const user = this.currentUser();
    if (!user) {
      return null;
    }
    try {
      const text = await firstValueFrom(
        this.http.get(`${API_BASE}/api/notifications/status`, { responseType: 'text' }),
      );
      return {
        id: 'notification-service-status',
        userId: user.id,
        type: 'NOTIFICATION_SERVICE',
        text,
        createdAt: new Date().toISOString(),
        read: true,
      };
    } catch {
      return null;
    }
  }

  private async runBackend<T>(operation: () => Promise<T>, fallback?: T): Promise<T> {
    this.busy.set(true);
    this.backendMessage.set('');
    try {
      return await operation();
    } catch (error) {
      this.backendMessage.set(this.describeError(error));
      if (fallback !== undefined) {
        return fallback;
      }
      throw error;
    } finally {
      this.busy.set(false);
    }
  }

  private headers(): HttpHeaders {
    return this.authHeaders(this.state().sessionToken);
  }

  private authHeaders(token: string): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  private authPayload(email: string, password: string, role: UserRole | string): { email: string; password: string; role: string } {
    return { email, password, role: this.toBackendRole(role) };
  }

  private toBackendRole(role: UserRole | string): string {
    const normalized = String(role)
      .trim()
      .toUpperCase()
      .replace(/^ROLE_/, '')
      .replace(/[^A-Z]/g, '');
    return normalized === 'COFUNDER' || normalized === 'COFOUNDER' ? 'COFOUNDER' : normalized;
  }

  private decodeJwt(token: string): { sub: string; roles?: string[] } {
    const payload = token.split('.')[1] ?? '';
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const decoded = atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '='));
    return JSON.parse(decoded);
  }

  private normalizeRole(role: string): UserRole {
    const normalized = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    return this.roles.includes(normalized as UserRole) ? (normalized as UserRole) : 'ROLE_FOUNDER';
  }

  private fromBackendStartup(startup: BackendStartup): Startup {
    const status = this.toListingStatus(startup.status);
    return {
      id: String(startup.id),
      founderId: String(startup.userId),
      startupName: startup.title,
      description: startup.description,
      industry: startup.domain,
      problemStatement: startup.problemStatement ?? startup.description,
      solution: startup.solution ?? startup.description,
      fundingGoal: Number(startup.fundingGoal ?? 5000000),
      stage: this.toStartupStage(startup.stage, status),
      location: startup.location ?? 'India',
      pitch: startup.pitch ?? startup.description,
      listingStatus: status,
      progress: 0,
      followers: [],
      teamRoles: this.toList(startup.teamRoles ?? 'CTO, CPO'),
      createdAt: new Date().toISOString(),
    };
  }

  private toBackendStartupPayload(draft: ReturnType<typeof this.startupDraft>, userId: string): Partial<BackendStartup> {
    return {
      title: draft.startupName,
      description: draft.description || draft.solution || draft.pitch,
      domain: draft.industry,
      status: draft.id ? 'PENDING' : 'PENDING',
      userId: Number(userId),
      problemStatement: draft.problemStatement,
      solution: draft.solution,
      fundingGoal: Number(draft.fundingGoal),
      stage: draft.stage,
      location: draft.location,
      pitch: draft.pitch,
      teamRoles: draft.teamRoles,
    };
  }

  private fromBackendInvestment(investment: BackendInvestment): Investment {
    return {
      id: String(investment.id),
      startupId: String(investment.startupId),
      investorId: String(investment.investorId),
      amount: Number(investment.amount),
      status: this.toInvestmentStatus(investment.status),
      createdAt: new Date().toISOString(),
    };
  }

  private fromBackendTeam(team: BackendTeam): TeamInvite {
    return {
      id: String(team.id),
      startupId: String(team.startupId),
      invitedUserId: String(team.userId),
      role: team.role,
      status: team.status === 'ACTIVE' ? 'ACCEPTED' : 'PENDING',
      createdAt: new Date().toISOString(),
    };
  }

  private fromBackendMessage(message: BackendMessage, receiverId: string): Message {
    return {
      id: String(message.id),
      conversationId: String(message.conversationId),
      senderId: String(message.senderId),
      receiverId,
      content: message.content,
      createdAt: message.timestamp ?? new Date().toISOString(),
    };
  }

  private partnerIdForConversation(conversation: BackendConversation, senderId: number): string {
    return String(conversation.user1Id === senderId ? conversation.user2Id : conversation.user1Id);
  }

  private toUser(profile: BackendUserProfile, role: UserRole, password: string): User {
    return {
      id: String(profile.id),
      role,
      password,
      profile: {
        name: profile.name,
        email: profile.email,
        skills: this.toList(profile.skills ?? ''),
        experience: profile.experience ?? '',
        bio: profile.bio ?? '',
        portfolioLinks: this.toList(profile.portfolioLinks ?? ''),
        location: profile.location ?? 'India',
      },
    };
  }

  private toListingStatus(status: string): ListingStatus {
    if (status?.toUpperCase() === 'ACCEPTED' || status?.toUpperCase() === 'APPROVED') {
      return 'APPROVED';
    }
    if (status?.toUpperCase() === 'REJECTED') {
      return 'REJECTED';
    }
    return 'PENDING_REVIEW';
  }

  private toInvestmentStatus(status: string): InvestmentStatus {
    const normalized = status?.toUpperCase() as InvestmentStatus;
    return this.investmentStatuses.includes(normalized) ? normalized : 'PENDING';
  }

  private toStartupStage(stage: string | undefined, status: ListingStatus): StartupStage {
    const normalized = stage?.toUpperCase() as StartupStage;
    if (this.startupStages.includes(normalized)) {
      return normalized;
    }
    return status === 'APPROVED' ? 'EARLY_TRACTION' : 'MVP';
  }

  private upsertUser(users: User[], user: User): User[] {
    return users.some((item) => item.id === user.id)
      ? users.map((item) => (item.id === user.id ? user : item))
      : [...users, user];
  }

  private upsertStartup(startups: Startup[], startup: Startup): Startup[] {
    return startups.some((item) => item.id === startup.id)
      ? startups.map((item) => (item.id === startup.id ? startup : item))
      : [startup, ...startups];
  }

  private upsertInvestment(investments: Investment[], investment: Investment): Investment[] {
    return investments.some((item) => item.id === investment.id)
      ? investments.map((item) => (item.id === investment.id ? investment : item))
      : [investment, ...investments];
  }

  private upsertInvite(invites: TeamInvite[], invite: TeamInvite): TeamInvite[] {
    return invites.some((item) => item.id === invite.id)
      ? invites.map((item) => (item.id === invite.id ? invite : item))
      : [invite, ...invites];
  }

  private upsertNotification(notifications: NotificationItem[], notification: NotificationItem): NotificationItem[] {
    return notifications.some((item) => item.id === notification.id)
      ? notifications.map((item) => (item.id === notification.id ? notification : item))
      : [notification, ...notifications];
  }

  private pushNotification(type: string, text: string): void {
    const user = this.currentUser();
    if (!user) {
      return;
    }
    this.pushNotificationForUser(user.id, type, text);
  }

  private pushNotificationForUser(userId: string, type: string, text: string): void {
    const notification: NotificationItem = {
      id: `${type}-${Date.now()}`,
      userId,
      type,
      text,
      createdAt: new Date().toISOString(),
      read: false,
    };
    this.state.update((state) => ({ ...state, notifications: [notification, ...state.notifications] }));
  }

  private formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(amount);
  }

  private loadState(): AppState {
    if (typeof window === 'undefined') {
      return this.emptyState([]);
    }
    try {
      const stored = JSON.parse(window.localStorage.getItem(STORAGE_KEY) ?? '{}') as Partial<AppState>;
      return {
        ...this.emptyState(stored.users ?? []),
        startups: stored.startups ?? [],
        investments: stored.investments ?? [],
        invites: stored.invites ?? [],
        messages: stored.messages ?? [],
        notifications: stored.notifications ?? [],
        sessionUserId: stored.sessionUserId ?? '',
        sessionToken: stored.sessionToken ?? '',
      };
    } catch {
      return this.emptyState([]);
    }
  }

  private emptyState(users: User[]): AppState {
    return {
      users,
      startups: [],
      investments: [],
      invites: [],
      messages: [],
      notifications: [],
      sessionUserId: '',
      sessionToken: '',
    };
  }

  private roleForEmail(email: string): UserRole {
    if (email.toLowerCase().startsWith('admin')) {
      return 'ROLE_ADMIN';
    }
    return 'ROLE_FOUNDER';
  }

  private canMessageRole(senderRole: UserRole, receiverRole: UserRole): boolean {
    const allowed: Record<UserRole, UserRole[]> = {
      ROLE_FOUNDER: ['ROLE_INVESTOR', 'ROLE_COFOUNDER', 'ROLE_ADMIN'],
      ROLE_INVESTOR: ['ROLE_FOUNDER'],
      ROLE_COFOUNDER: ['ROLE_FOUNDER'],
      ROLE_ADMIN: ['ROLE_FOUNDER'],
    };
    return allowed[senderRole].includes(receiverRole);
  }

  private getUserById(userId: string): User | undefined {
    return this.state().users.find((user) => user.id === userId);
  }

  private getStartupById(startupId: string): Startup | undefined {
    return this.state().startups.find((startup) => startup.id === startupId);
  }

  private toList(value: string): string[] {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }

  private emptyProfileDraft(): UserProfile {
    return {
      name: '',
      email: '',
      skills: [],
      experience: '',
      bio: '',
      portfolioLinks: [],
      location: '',
    };
  }

  private describeError(error: unknown): string {
    if (typeof error === 'object' && error && 'error' in error) {
      const body = (error as { error?: unknown }).error;
      if (typeof body === 'string') {
        return this.cleanBackendMessage(body);
      }
      if (typeof body === 'object' && body && 'message' in body) {
        return this.cleanBackendMessage(String((body as { message: unknown }).message));
      }
    }
    if (typeof error === 'object' && error && 'message' in error) {
      return this.cleanBackendMessage(String((error as { message: unknown }).message));
    }
    return 'Backend request failed. Check that Docker services are running.';
  }

  private cleanBackendMessage(message: string): string {
    const trimmed = message.trim();
    if (trimmed.startsWith('{')) {
      try {
        const parsed = JSON.parse(trimmed) as { message?: string };
        return this.cleanBackendMessage(parsed.message ?? trimmed);
      } catch {
        return 'Request failed. Please check your details and try again.';
      }
    }
    if (trimmed === 'Selected role does not match this account') {
      return 'Selected role does not match this account.';
    }
    if (trimmed === 'Invalid password') {
      return 'Password is incorrect.';
    }
    if (trimmed === 'User not found') {
      return 'Email address was not found.';
    }
    return trimmed;
  }
}
